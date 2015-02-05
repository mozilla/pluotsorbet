.class public gnu/testlet/vm/TestTwoFieldsSameName
.super java/lang/Object
.implements gnu/testlet/Testlet

.field private foo I
.field private foo Z

.method public <init>()V
   aload_0

   invokenonvirtual java/lang/Object/<init>()V
   return
.end method

.method public getExpectedPass()I
   iconst_1
   ireturn
.end method

.method public getExpectedFail()I
   iconst_0
   ireturn
.end method

.method public getExpectedKnownFail()I
   iconst_0
   ireturn
.end method

.method public test(Lgnu/testlet/TestHarness;)V
   .limit stack 2
   .limit locals 2

   ; Store 1 in the boolean variable foo
   aload_0
   iconst_1
   putfield gnu/testlet/vm/TestTwoFieldsSameName/foo Z

	 ; Store 0 in the integer variable foo
	 aload_0
   iconst_0
   putfield gnu/testlet/vm/TestTwoFieldsSameName/foo I

   aload_1
   aload_0
   getfield gnu/testlet/vm/TestTwoFieldsSameName/foo Z
   invokevirtual gnu/testlet/TestHarness/check(Z)V

   return
.end method
