The *buildtime/* and *runtime/* subdirectories of this directory support tests
that need to be built with a different set of classes than the ones they access
at runtime.

For example, the `gnu.testlet.vm.MethodNotFoundException` test calls
`org.mozilla.test.ClassWithMissingMethod.missingMethod()`, which needs to exist
at buildtime to successfully compile the class, but which shouldn't exist
at runtime in order to test that an exception is correctly raised.

Put the buildtime implementations of any such classes into the *buildtime/*
subdirectory and the runtime implementations into *runtime/*.
