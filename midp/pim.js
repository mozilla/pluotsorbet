/* -*- Mode: Java; tab-width: 2; indent-tabs-mode: nil; c-basic-offset: 2 -*- */
/* vim: set shiftwidth=2 tabstop=2 autoindent cindent expandtab: */

'use strict';

var PIM = {};
PIM.CONTACT_LIST = 1;
PIM.EVENT_LIST = 2;
PIM.TODO_LIST = 3;

Native.create("com/sun/j2me/pim/PIMProxy.getListNamesCount0.(I)I", function(listType) {
  console.warn("PIMProxy.getListNamesCount0.(I)I not implemented");

  if (listType === PIM.CONTACT_LIST) {
    return 1;
  }

  return 0;
});

Native.create("com/sun/j2me/pim/PIMProxy.getListNames0.([Ljava/lang/String;)V", function(names) {
  console.warn("PIMProxy.getListNames0.([Ljava/lang/String;)V not implemented");
  names[0] = util.newString("Contacts");
});

Native.create("com/sun/j2me/pim/PIMProxy.listOpen0.(ILjava/lang/String;I)I", function(listType, listName, mode) {
  console.warn("PIMProxy.listOpen0.(ILjava/lang/String;I)I not implemented");
  return 1;
});
