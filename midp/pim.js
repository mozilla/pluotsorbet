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
  names[0] = util.newString("ContactList");
});

Native.create("com/sun/j2me/pim/PIMProxy.listOpen0.(ILjava/lang/String;I)I", function(listType, listName, mode) {
  console.warn("PIMProxy.listOpen0.(ILjava/lang/String;I)I not implemented");
  return 1;
});

Native.create("com/sun/j2me/pim/PIMProxy.getNextItemDescription0.(I[I)Z", function(listHandle, description) {
  console.warn("PIMProxy.getNextItemDescription0.(I[I)Z not implemented");
  return false;
});

Native.create("com/sun/j2me/pim/PIMProxy.listClose0.(I)Z", function(listHandle, description) {
  console.warn("PIMProxy.listClose0.(I)Z not implemented");
  return true;
});

Native.create("com/sun/j2me/pim/PIMProxy.getDefaultListName.(I)Ljava/lang/String;", function(listType) {
  console.warn("PIMProxy.getDefaultListName.(I)Ljava/lang/String; not implemented");
  if (listType === PIM.CONTACT_LIST) {
    return "ContactList";
  } else if (listType === PIM.EVENT_LIST) {
    return "EventList";
  } else if (listType === PIM.TODO_LIST) {
    return "TodoList";
  }
});

Native.create("com/sun/j2me/pim/PIMProxy.getFieldsCount0.(I[I)I", function(listHandle, dataHandle) {
  console.warn("PIMProxy.getFieldsCount0.(I[I)I not implemented");
  if (listHandle === 1) {
    return 2;
  } else {
    return 0;
  }
});

Native.create("com/sun/j2me/pim/PIMProxy.getFieldLabelsCount0.(III)I", function(listHandle, fieldIndex, dataHandle) {
  console.warn("PIMProxy.getFieldLabelsCount0.(III)I not implemented");

  if (listHandle === 1) {
    return 1;
  } else {
    return 0;
  }
});

Native.create("com/sun/j2me/pim/PIMProxy.getFields0.(I[Lcom/sun/j2me/pim/PIMFieldDescriptor;I)V", function(listHandle, desc, dataHandle) {
  console.warn("PIMProxy.getFieldLabelsCount0.(III)I not implemented");

  if (listHandle !== 1) {
    return;
  }

  return 0;
});
