module J2ME.Bytecode {
  import assert = Debug.assert;
  import Uint32ArrayBitSet = BitSets.Uint32ArrayBitSet;

  var writer = new IndentingWriter();

  export class Block {
    public startBci: number;
    public endBci: number;
    public isExceptionEntry: boolean;
    public isLoopHeader: boolean;
    public isLoopEnd: boolean;
    public hasHandlers: boolean;
    public blockID: number;

    public region: any;

    public successors: Block [];
    public normalSuccessors: number;
    public numberOfPredecessors: number = 0;

    visited: boolean;
    active: boolean;
    public loops: number = 0; // long
    public exits: number = 0; // long
    public loopID: number = -1; // long

    constructor() {
      this.successors = [];
    }

    public clone(): Block {
      var block = new Block();
      block.startBci = this.startBci;
      block.endBci = this.endBci;
      block.isExceptionEntry = this.isExceptionEntry;
      block.isLoopHeader = this.isLoopHeader;
      block.isLoopEnd = this.isLoopEnd;
      block.hasHandlers = this.hasHandlers;
      block.loops = this.loops;
      block.loopID = this.loopID;
      block.blockID = this.blockID;
      block.successors = this.successors.slice(0);
      block.numberOfPredecessors = this.numberOfPredecessors;
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
      this.fixLoopBits();
      this.initializeBlockIDs();
      this.computeLoopStores();
    }

    private makeExceptionEntries() {
      // start basic blocks at all exception handler blocks and mark them as exception entries
      for (var i = 0; i < this.exceptionHandlers.length; i++) {
        var handler = this.exceptionHandlers[i];
        var block = this.makeBlock(handler.handler_pc);
        block.isExceptionEntry = true;
      }
    }

    private computeLoopStores() {

    }

    private initializeBlockIDs() {
      for (var i = 0; i < this.blocks.length; i++) {
        this.blocks[i].blockID = i;
      }
    }

    public getBlock(bci: number) {
      return this.blockMap[bci];
    }

    private makeBlock(startBci: number): Block {
      var oldBlock = this.blockMap[startBci];
      if (!oldBlock) {
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
      return Bytecode.canTrap(opcode);
    }

    private iterateOverBytecodes() {
      // iterate over the bytecodes top to bottom.
      // mark the entrypoints of basic blocks and build lists of successors for
      // all bytecodes that end basic blocks (i.e. goto, ifs, switches, throw, jsr, returns, ret)
      var code = this.method.code;
      var current: Block = null;
      var bci = 0;
      while (bci < code.length) {
        if (!current || this.blockMap[bci]) {
          var b = this.makeBlock(bci);
          if (current) {
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
                current = null;
                break;
              }
            }
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
    private _nextLoop = 0;

    /**
     * Mark the block as a loop header, using the next available loop number.
     * Also checks for corner cases that we don't want to compile.
     */
    private makeLoopHeader(block: Block) {
      if (!block.isLoopHeader) {
        block.isLoopHeader = true;
        if (block.isExceptionEntry) {
          // Loops that are implicitly formed by an exception handler lead to all sorts of corner cases.
          // However, this doesn't affect the baseline JIT, so don't bail out.
          // TODO: Revisit for OPT JIT.
        }
        if (this._nextLoop >= 32) {
          // This restriction can be removed by using a fall-back to a BitSet in case we have more than 32 loops
          // Don't compile such methods for now, until we see a concrete case that allows checking for correctness.
          throw "Too many loops in method";
        }
        assert (!block.loops, block.loops);
        block.loops = 1 << this._nextLoop;
        block.loopID = this._nextLoop;
        this._nextLoop ++;
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
        if (handlers) {
          var dispatch = this.makeExceptionDispatch(handlers, 0, bci);
          block.successors.push(dispatch);
          block.hasHandlers = true;
        }
      }
    }


    private fixLoopBits() {
      var loopChanges: boolean = false;
      function _fixLoopBits(block: Block) {
        if (block.visited) {
          // Return cached loop information for this block.
          if (block.isLoopHeader) {
            return block.loops & ~(1 << block.loopID);
          } else {
            return block.loops;
          }
        }

        block.visited = true;
        var loops = block.loops;
        var successors = block.successors;
        for (var i = 0; i < successors.length; i++) {
          // Recursively process successors.
          loops |= _fixLoopBits(successors[i]);
        }
        for (var i = 0; i < successors.length; i++) {
          var successor = successors[i];
          successor.exits = loops & ~successor.loops;
        }
        if (block.loops !== loops) {
          loopChanges = true;
          block.loops = loops;
        }

        if (block.isLoopHeader) {
          loops &= ~(1 << block.loopID);
        }
        return loops;
      }

      do {
        loopChanges = false;
        for (var i = 0; i < this.blocks.length; i++) {
          this.blocks[i].visited = false;
        }
        var loop = _fixLoopBits(this.blockMap[0]);
        if (loop !== 0) {
          // There is a path from a loop end to the method entry that does not pass the loop
          // header.
          // Therefore, the loop is non reducible (has more than one entry).
          // We don't want to compile such methods because the IR only supports structured
          // loops.
          throw new CompilerBailout("Non-reducible loop");
        }
      } while (loopChanges);
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
          return block.loops;
        } else if (block.isLoopHeader) {
          return block.loops & ~(1 << block.loopID);
        } else {
          return block.loops;
        }
      }

      block.visited = true;
      block.active = true;

      var loops = 0;

      for (var i = 0; i < block.successors.length; i++) {
        var successor = block.successors[i];
        successor.numberOfPredecessors ++;
        // Recursively process successors.
        loops |= this.computeBlockOrderFrom(block.successors[i]);
        if (successor.active) {
          // Reached block via backward branch.
          block.isLoopEnd = true;
        }
      }

      block.loops = loops;

      if (block.isLoopHeader) {
        loops &= ~(1 << block.loopID);
      }

      block.active = false;
      this.blocks.push(block);
      return loops;
    }

    public blockToString(block: Block): string {
      return "blockID: " + String(block.blockID +
      ", ").padRight(" ", 5) +
      "bci: [" + block.startBci + ", " + block.endBci + "]" +
      (block.successors.length ? ", successors: => " + block.successors.map(b => b.blockID).join(", ") : "") +
      (block.isLoopHeader ? " isLoopHeader" : "") +
      (block.isLoopEnd ? " isLoopEnd" : "") +
      (block.isExceptionEntry ? " isExceptionEntry" : "") +
      (block.hasHandlers ? " hasHandlers" : "") +
      ", loops: " + block.loops.toString(2) +
      ", exits: " + block.exits.toString(2) +
      ", loopID: " + block.loopID;
    }

    public trace(writer: IndentingWriter, traceBytecode: boolean = false) {
      var code = this.method.code;
      var stream = new BytecodeStream(code);

      writer.enter("Block Map: " + this.blocks.map(b => b.blockID).join(", "));
      this.blocks.forEach(block => {
        writer.enter(this.blockToString(block));
        if (traceBytecode) {
          var bci = block.startBci;
          stream.setBCI(bci);
          while (stream.currentBCI <= block.endBci) {
            writer.writeLn(Bytecodes[stream.currentBC()]);
            stream.next();
            bci = stream.currentBCI;
          }
        }
        writer.outdent();
      });
      writer.outdent();
    }
  }
}
