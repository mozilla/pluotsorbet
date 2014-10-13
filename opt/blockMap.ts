module J2ME.Bytecode {
  import assert = Debug.assert;
  import Uint32ArrayBitSet = BitSets.Uint32ArrayBitSet;

  var writer = new IndentingWriter();

  export class Block {
    public startBci: number;
    public endBci: number;
    public isExceptionEntry: boolean;
    public isLoopHeader: boolean;
    public blockID: number;

    // public FixedWithNextNode firstInstruction;

    public successors: Block [];
    public normalSuccessors: number;

    visited: boolean;
    active: boolean;
    public loops: number; // long

    constructor() {
      this.successors = [];
    }

    public clone(): Block {
      var block = new Block();
      block.startBci = this.startBci;
      block.endBci = this.endBci;
      block.isExceptionEntry = this.isExceptionEntry;
      block.isLoopHeader = this.isLoopHeader;
      block.blockID = this.blockID;
      block.successors = this.successors.slice(0);
      return block;
    }
  }

  export class ExceptionBlock extends Block {
    public handler: ExceptionHandler ;
    public deoptBci: number;
  }

  export class BlockMap {
    method: MethodInfo;
    blocks: Block [];
    private blockMap: Block [];
    private startBlock: Block;
    private canTrap: Uint32ArrayBitSet;
    // handlers: ExceptionHandler [];
    exceptionHandlers: ExceptionHandler [];

    constructor(method: MethodInfo) {
      this.blocks = [];
      this.method = method;
      this.blockMap = new Array<Block>(method.code.length);
      this.canTrap = new Uint32ArrayBitSet(this.blockMap.length);
      this.exceptionHandlers = this.method.exception_table;
    }

    build() {
      this.makeExceptionEntries();
      this.iterateOverBytecodes();
      this.addExceptionEdges();
      this.computeBlockOrder();
      this.initializeBlockIDs();
      // writer.writeLn("Blocks: " + this.blocks.length);
      // writer.writeLn(JSON.stringify(this.blocks, null, 2));
    }

    private makeExceptionEntries() {
      // start basic blocks at all exception handler blocks and mark them as exception entries
      for (var i = 0; i < this.exceptionHandlers.length; i++) {
        var handler = this.exceptionHandlers[i];
        var block = this.makeBlock(handler.handler_pc);
        block.isExceptionEntry = true;
      }
    }

    private initializeBlockIDs() {
      for (var i = 0; i < this.blocks.length; i++) {
        this.blocks[i].blockID = i;
      }
    }

    private makeBlock(startBci: number): Block {
      var oldBlock = this.blockMap[startBci];
      if (oldBlock == null) {
        var newBlock = new Block();
        newBlock.startBci = startBci;
        this.blockMap[startBci] = newBlock;
        return newBlock;
      } else if (oldBlock.startBci != startBci) {
        // Backward branch into the middle of an already processed block.
        // Add the correct fall-through successor.
        var newBlock = new Block();
        newBlock.startBci = startBci;
        newBlock.endBci = oldBlock.endBci;
        ArrayUtilities.pushMany(newBlock.successors, oldBlock.successors);
        newBlock.normalSuccessors = oldBlock.normalSuccessors;

        oldBlock.endBci = startBci - 1;
        oldBlock.successors.length = 0;
        oldBlock.successors.push(newBlock);
        oldBlock.normalSuccessors = 1;

        for (var i = startBci; i <= newBlock.endBci; i++) {
          this.blockMap[i] = newBlock;
        }
        return newBlock;
      } else {
        return oldBlock;
      }
    }

    private makeSwitchSuccessors(tswitch: BytecodeSwitch): Block [] {
      var max = tswitch.numberOfCases();
      var successors = new Array<Block>(max + 1);
      for (var i = 0; i < max; i++) {
          successors[i] = this.makeBlock(tswitch.targetAt(i));
      }
      successors[max] = this.makeBlock(tswitch.defaultTarget());
      return successors;
    }

    private setSuccessors(predBci: number, successors: Block []) {
      // writer.writeLn("setSuccessors " + predBci + " " + successors.map(x => x.startBci).join(", "));
      var predecessor = this.blockMap[predBci];
      assert (predecessor.successors.length === 0, predecessor.successors.map(x => x.startBci).join(", "));
      ArrayUtilities.pushMany(predecessor.successors, successors);
      predecessor.normalSuccessors = successors.length;
    }

    public canTrapAt(opcode: Bytecodes, bci: number): boolean {
      switch (opcode) {
        case Bytecodes.INVOKESTATIC:
        case Bytecodes.INVOKESPECIAL:
        case Bytecodes.INVOKEVIRTUAL:
        case Bytecodes.INVOKEINTERFACE:
          return true;
        case Bytecodes.IASTORE:
        case Bytecodes.LASTORE:
        case Bytecodes.FASTORE:
        case Bytecodes.DASTORE:
        case Bytecodes.AASTORE:
        case Bytecodes.BASTORE:
        case Bytecodes.CASTORE:
        case Bytecodes.SASTORE:
        case Bytecodes.IALOAD:
        case Bytecodes.LALOAD:
        case Bytecodes.FALOAD:
        case Bytecodes.DALOAD:
        case Bytecodes.AALOAD:
        case Bytecodes.BALOAD:
        case Bytecodes.CALOAD:
        case Bytecodes.SALOAD:
        case Bytecodes.PUTFIELD:
        case Bytecodes.GETFIELD:
          return false; // ???
      }
      return false;
    }

    private iterateOverBytecodes() {
      // iterate over the bytecodes top to bottom.
      // mark the entrypoints of basic blocks and build lists of successors for
      // all bytecodes that end basic blocks (i.e. goto, ifs, switches, throw, jsr, returns, ret)
      var code = this.method.code;
      var current: Block = null;
      var bci = 0;
      while (bci < code.length) {
        if (current == null || this.blockMap[bci] != null) {
          var b = this.makeBlock(bci);
          if (current != null) {
            this.setSuccessors(current.endBci, [b]);
          }
          current = b;
        }
        this.blockMap[bci] = current;
        current.endBci = bci;

        var opcode = Bytes.beU1(code, bci);
        switch (opcode) {
          case Bytecodes.IRETURN: // fall through
          case Bytecodes.LRETURN: // fall through
          case Bytecodes.FRETURN: // fall through
          case Bytecodes.DRETURN: // fall through
          case Bytecodes.ARETURN: // fall through
          case Bytecodes.RETURN: {
            current = null;
            break;
          }
          case Bytecodes.ATHROW: {
            current = null;
            this.canTrap.set(bci);
            break;
          }
          case Bytecodes.IFEQ:      // fall through
          case Bytecodes.IFNE:      // fall through
          case Bytecodes.IFLT:      // fall through
          case Bytecodes.IFGE:      // fall through
          case Bytecodes.IFGT:      // fall through
          case Bytecodes.IFLE:      // fall through
          case Bytecodes.IF_ICMPEQ: // fall through
          case Bytecodes.IF_ICMPNE: // fall through
          case Bytecodes.IF_ICMPLT: // fall through
          case Bytecodes.IF_ICMPGE: // fall through
          case Bytecodes.IF_ICMPGT: // fall through
          case Bytecodes.IF_ICMPLE: // fall through
          case Bytecodes.IF_ACMPEQ: // fall through
          case Bytecodes.IF_ACMPNE: // fall through
          case Bytecodes.IFNULL:    // fall through
          case Bytecodes.IFNONNULL: {
            current = null;
            var probability = -1;
            var b1 = this.makeBlock(bci + Bytes.beS2(code, bci + 1));
            var b2 = this.makeBlock(bci + 3);
            this.setSuccessors(bci, [b1, b2]);
            break;
          }
          case Bytecodes.GOTO:
          case Bytecodes.GOTO_W: {
            current = null;
            var target = bci + Bytes.beSVar(code, bci + 1, opcode == Bytecodes.GOTO_W);
            var b1 = this.makeBlock(target);
            this.setSuccessors(bci, [b1]);
            break;
          }
          case Bytecodes.TABLESWITCH: {
            current = null;
            this.setSuccessors(bci, this.makeSwitchSuccessors(new BytecodeTableSwitch(code, bci)));
            break;
          }
          case Bytecodes.LOOKUPSWITCH: {
            current = null;
            this.setSuccessors(bci, this.makeSwitchSuccessors(new BytecodeLookupSwitch(code, bci)));
            break;
          }
          case Bytecodes.WIDE: {
            var opcode2 = Bytes.beU1(code, bci);
            switch (opcode2) {
              case Bytecodes.RET: {
                writer.writeLn("RET");
                // current.endsWithRet = true;
                current = null;
                break;
              }
            }
            break;
          }
          case Bytecodes.INVOKEINTERFACE:
          case Bytecodes.INVOKESPECIAL:
          case Bytecodes.INVOKESTATIC:
          case Bytecodes.INVOKEVIRTUAL: {
            current = null;
            var target = bci + lengthAt(code, bci);
            var b1 = this.makeBlock(target);
            this.setSuccessors(bci, [b1]);
            this.canTrap.set(bci);
            break;
          }
          default: {
            if (this.canTrapAt(opcode, bci)) {
              this.canTrap.set(bci);
            }
          }
        }
        bci += lengthAt(code, bci);
      }
    }

    /**
     * The next available loop number.
     */
    private nextLoop = 0;

    /**
     * Mark the block as a loop header, using the next available loop number.
     * Also checks for corner cases that we don't want to compile.
     */
    private makeLoopHeader(block: Block) {
      if (!block.isLoopHeader) {
        block.isLoopHeader = true;
        if (block.isExceptionEntry) {
          // Loops that are implicitly formed by an exception handler lead to all sorts of corner cases.
          // Don't compile such methods for now, until we see a concrete case that allows checking for correctness.
          throw new CompilerBailout("Loop formed by an exception handler");
        }
        if (this.nextLoop >= 32) {
          // This restriction can be removed by using a fall-back to a BitSet in case we have more than 32 loops
          // Don't compile such methods for now, until we see a concrete case that allows checking for correctness.
          throw "Too many loops in method";
        }
        assert (block.loops === 0);
        block.loops = 1 << this.nextLoop;
        this.nextLoop ++;
      }
      assert (IntegerUtilities.bitCount(block.loops) === 1);
    }

    // catch_type

    private handlerIsCatchAll(handler: ExceptionHandler) {
      return handler.catch_type === 0;
    }

    private exceptionDispatch: Map<ExceptionHandler, ExceptionBlock> = new Map<ExceptionHandler, ExceptionBlock>();

    private makeExceptionDispatch(handlers: ExceptionHandler [], index: number, bci: number): Block {
      var handler = handlers[index];
      if (this.handlerIsCatchAll(handler)) {
        return this.blockMap[handler.handler_pc];
      }
      var block = this.exceptionDispatch.get(handler);
      if (!block) {
        block = new ExceptionBlock();
        block.startBci = -1;
        block.endBci = -1;
        block.deoptBci = bci;
        block.handler = handler;
        block.successors.push(this.blockMap[handler.handler_pc]);
        if (index < handlers.length - 1) {
          block.successors.push(this.makeExceptionDispatch(handlers, index + 1, bci));
        }
        this.exceptionDispatch.set(handler, block);
      }
      return block;
    }

    private addExceptionEdges() {
      var length = this.canTrap.length;
      for (var bci = this.canTrap.nextSetBit(0, length); bci >= 0; bci = this.canTrap.nextSetBit(bci + 1, length)) {
        var block = this.blockMap[bci];
        var handlers: ExceptionHandler [] = null;
        for (var i = 0; i < this.exceptionHandlers.length; i++) {
          var handler = this.exceptionHandlers[i];
          if (handler.start_pc <= bci && bci < handler.end_pc) {
            if (!handlers) {
              handlers = [];
            }
            handlers.push(handler);
            if (this.handlerIsCatchAll(handler)) {
              break;
            }
          }
        }
        if (handlers !== null) {
          var dispatch = this.makeExceptionDispatch(handlers, 0, bci);
          block.successors.push(dispatch);
        }
      }
    }

    private computeBlockOrder() {
      var loop = this.computeBlockOrderFrom(this.blockMap[0]);
      if (loop != 0) {
        // There is a path from a loop end to the method entry that does not pass the loop header.
        // Therefore, the loop is non reducible (has more than one entry).
        // We don't want to compile such methods because the IR only supports structured loops.
        throw new CompilerBailout("Non-reducible loop");
      }
      // Convert postorder to the desired reverse postorder.
      this.blocks.reverse();
    }

    /**
     * Depth-first traversal of the control flow graph. The flag {@linkplain Block#visited} is used to
     * visit every block only once. The flag {@linkplain Block#active} is used to detect cycles (backward
     * edges).
     */
    private computeBlockOrderFrom(block: Block): number {
      if (block.visited) {
        if (block.active) {
          // Reached block via backward branch.
          this.makeLoopHeader(block);
        }
        // Return cached loop information for this block.
        return block.loops;
      }

      block.visited = true;
      block.active = true;

      var loops = 0;

      for (var i = 0; i < block.successors.length; i++) {
        // Recursively process successors.
        loops |= this.computeBlockOrderFrom(block.successors[i]);
      }

      if (block.isLoopHeader) {
        assert (IntegerUtilities.bitCount(block.loops) === 1);
        loops &= ~block.loops;
      }

      block.loops = loops;
      block.active = false;
      this.blocks.push(block);
      return loops;
    }
  }
}