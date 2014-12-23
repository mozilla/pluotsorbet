.PHONY: clean

SRC = src/Jasmin.java \
      src/jasmin/ClassFile.java \
      src/jasmin/InsnInfo.java \
      src/jasmin/Main.java \
      src/jasmin/num_token.java \
      src/jasmin/parser.java \
      src/jasmin/relative_num_token.java \
      src/jasmin/ReservedWords.java \
      src/jasmin/Scanner.java \
      src/jasmin/ScannerUtils.java \
      src/jasmin/sym.java \
      src/jasmin/var_token.java \
      src/jas/Annotation.java \
      src/jas/AnnotationAttr.java \
      src/jas/AnnotationElement.java \
      src/jas/AnnotDefAttr.java \
      src/jas/AnnotParamAttr.java \
      src/jas/AsciiCP.java \
      src/jas/CatchEntry.java \
      src/jas/Catchtable.java \
      src/jas/ClassCP.java \
      src/jas/ClassEnv.java \
      src/jas/CodeAttr.java \
      src/jas/ConstAttr.java \
      src/jas/CP.java \
      src/jas/DeprecatedAttr.java \
      src/jas/DoubleCP.java \
      src/jas/EnclosingMethodAttr.java \
      src/jas/ExceptAttr.java \
      src/jas/FieldCP.java \
      src/jas/FloatCP.java \
      src/jas/GenericAttr.java \
      src/jas/IincInsn.java \
      src/jas/InnerClass.java \
      src/jas/InnerClassesAttr.java \
      src/jas/Insn.java \
      src/jas/InsnOperand.java \
      src/jas/IntegerCP.java \
      src/jas/InterfaceCP.java \
      src/jas/InvokeinterfaceInsn.java \
      src/jas/jasError.java \
      src/jas/Label.java \
      src/jas/LabelOrOffset.java \
      src/jas/LineTableAttr.java \
      src/jas/LocalVarEntry.java \
      src/jas/LocalVarTableAttr.java \
      src/jas/LocalVarTypeTableAttr.java \
      src/jas/LongCP.java \
      src/jas/LookupswitchInsn.java \
      src/jas/Method.java \
      src/jas/MethodCP.java \
      src/jas/MultiarrayInsn.java \
      src/jas/NameTypeCP.java \
      src/jas/RuntimeConstants.java \
      src/jas/SignatureAttr.java \
      src/jas/SourceAttr.java \
      src/jas/SourceDebugExtensionAttr.java \
      src/jas/StackMap.java \
      src/jas/StringCP.java \
      src/jas/TableswitchInsn.java \
      src/jas/Var.java \
      src/jas/VerificationTypeInfo.java \
      src/jas/VerifyFrame.java \
      src/java_cup/runtime/char_token.java \
      src/java_cup/runtime/double_token.java \
      src/java_cup/runtime/float_token.java \
      src/java_cup/runtime/int_token.java \
      src/java_cup/runtime/long_token.java \
      src/java_cup/runtime/lr_parser.java \
      src/java_cup/runtime/str_token.java \
      src/java_cup/runtime/symbol.java \
      src/java_cup/runtime/token.java \
      src/java_cup/runtime/virtual_parse_stack.java \
      src/jasmin.mf

CUP = src/java_cup/Main.java \
      src/java_cup/action_part.java \
      src/java_cup/action_production.java \
      src/java_cup/emit.java \
      src/java_cup/internal_error.java \
      src/java_cup/lalr_item.java \
      src/java_cup/lalr_item_set.java \
      src/java_cup/lalr_state.java \
      src/java_cup/lalr_transition.java \
      src/java_cup/lexer.java \
      src/java_cup/lr_item_core.java \
      src/java_cup/non_terminal.java \
      src/java_cup/parse_action.java \
      src/java_cup/parse_action_row.java \
      src/java_cup/parse_action_table.java \
      src/java_cup/parse_reduce_row.java \
      src/java_cup/parse_reduce_table.java \
      src/java_cup/production.java \
      src/java_cup/production_part.java \
      src/java_cup/reduce_action.java \
      src/java_cup/shift_action.java \
      src/java_cup/symbol.java \
      src/java_cup/symbol_part.java \
      src/java_cup/symbol_set.java \
      src/java_cup/terminal.java \
      src/java_cup/terminal_set.java \
      src/java_cup/version.java \
      src/java_cup/runtime/lr_parser.java \
      src/java_cup/runtime/str_token.java \
      src/java_cup/runtime/symbol.java \
      src/java_cup/runtime/token.java \
      src/java_cup/runtime/virtual_parse_stack.java \
      src/java_cup.mf

all: jasmin.jar java_cup.jar

jasmin.jar : $(SRC)
	mkdir out
	javac -extdirs "" -source 1.2 -target 1.1 -d out -cp src src/Jasmin.java
	jar cfm jasmin.jar src/jasmin.mf -C out .
	rm -rf out

java_cup.jar : $(CUP)
	mkdir out_cup
	javac -extdirs "" -source 1.2 -target 1.1 -d out_cup -cp src src/java_cup/Main.java
	jar cfm java_cup.jar src/java_cup.mf -C out_cup .
	rm -rf out_cup

src/jasmin/parser.java:
src/jasmin/sym.java: src/jasmin/parser.cup java_cup.jar
	java -jar java_cup.jar -out src/jasmin -nosummary <src/jasmin/parser.cup

clean:
	rm -f jasmin.jar java_cup.jar src/jasmin/parser.java src/jasmin/sym.java
