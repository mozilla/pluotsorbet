.class public gnu/testlet/vm/TestJasminExample
.super java/lang/Object
.implements gnu/testlet/Testlet

.method public <init>()V
   aload_0

   invokenonvirtual java/lang/Object/<init>()V
   return
.end method


.method public test(Lgnu/testlet/TestHarness;)V
   .limit stack 2

   aload_1
   iconst_1
   invokevirtual gnu/testlet/TestHarness/check(Z)V

   return
.end method
