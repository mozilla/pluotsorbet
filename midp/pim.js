/* -*- Mode: Java; tab-width: 2; indent-tabs-mode: nil; c-basic-offset: 2 -*- */
/* vim: set shiftwidth=2 tabstop=2 autoindent cindent expandtab: */

'use strict';

var PIM = {};
PIM.CONTACT_LIST = 1;
PIM.EVENT_LIST = 2;
PIM.TODO_LIST = 3;

PIM.Contact = {
  FORMATTED_NAME: 105,
  TEL: 115,
  UID: 117,
};

PIM.PIMItem = {
  BINARY: 0,
  BOOLEAN: 1,
  DATE: 2,
  INT: 3,
  STRING: 4,
  STRING_ARRAY: 5,
};

PIM.lastListHandle = 0;
PIM.openLists = {};

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
  if (listType === PIM.CONTACT_LIST) {
    PIM.openLists[++PIM.lastListHandle] = {};
    return PIM.lastListHandle;
  }

  return 0;
});

Native.create("com/sun/j2me/pim/PIMProxy.getNextItemDescription0.(I[I)Z", function(listHandle, description) {
  console.warn("PIMProxy.getNextItemDescription0.(I[I)Z not implemented");

  return new Promise(function(resolve, reject) {
    contacts.getNext(function(contact) {
      if (contact == null) {
        resolve(false);
        return;
      }

      var str = '';

      contact2vcard.ContactToVcard([ contact ], function(vcards, nCards) {
        str += vcards;
      }, function() {
        PIM.curVcard = new TextEncoder('utf8').encode(str);

        description[0] = contact.id;
        description[1] = PIM.curVcard.byteLength;
        description[2] = 1;

        resolve(true);
      });
    });
  });
}, true);

Native.create("com/sun/j2me/pim/PIMProxy.getNextItemData0.(I[BI)Z", function(itemHandle, data, dataHandle) {
  console.warn("PIMProxy.getNextItemData0.(I[BI)Z not implemented");
  data.set(PIM.curVcard);
  return true;
});

Native.create("com/sun/j2me/pim/PIMProxy.getItemCategories0.(II)Ljava/lang/String;", function(itemHandle, dataHandle) {
  console.warn("PIMProxy.getItemCategories0.(II)Ljava/lang/String; not implemented");
  return null;
});

Native.create("com/sun/j2me/pim/PIMProxy.listClose0.(I)Z", function(listHandle, description) {
  console.warn("PIMProxy.listClose0.(I)Z not implemented");

  if (!(listHandle in PIM.openLists)) {
    return false;
  }

  delete PIM.openLists[listHandle];
  return true;
});

Native.create("com/sun/j2me/pim/PIMProxy.getDefaultListName.(I)Ljava/lang/String;", function(listType) {
  console.warn("PIMProxy.getDefaultListName.(I)Ljava/lang/String; not implemented");

  if (listType === PIM.CONTACT_LIST) {
    return "ContactList";
  }

  if (listType === PIM.EVENT_LIST) {
    return "EventList";
  }

  if (listType === PIM.TODO_LIST) {
    return "TodoList";
  }
});

Native.create("com/sun/j2me/pim/PIMProxy.getFieldsCount0.(I[I)I", function(listHandle, dataHandle) {
  console.warn("PIMProxy.getFieldsCount0.(I[I)I not implemented");
  return 3;
});

Native.create("com/sun/j2me/pim/PIMProxy.getFieldLabelsCount0.(III)I", function(listHandle, fieldIndex, dataHandle) {
  console.warn("PIMProxy.getFieldLabelsCount0.(III)I not implemented");
  return 1;
});

Native.create("com/sun/j2me/pim/PIMProxy.getFields0.(I[Lcom/sun/j2me/pim/PIMFieldDescriptor;I)V", function(listHandle, desc, dataHandle) {
  console.warn("PIMProxy.getFields0.(I[Lcom/sun/j2me/pim/PIMFieldDescriptor;I)V not implemented");

  desc[0].class.getField("I.field.I").set(desc[0], PIM.Contact.UID);
  desc[0].class.getField("I.dataType.I").set(desc[0], PIM.PIMItem.STRING);
  desc[0].class.getField("I.maxValues.I").set(desc[0], 1);
  desc[1].class.getField("I.field.I").set(desc[1], PIM.Contact.TEL);
  desc[1].class.getField("I.dataType.I").set(desc[1], PIM.PIMItem.STRING);
  desc[1].class.getField("I.maxValues.I").set(desc[1], -1);
  desc[2].class.getField("I.field.I").set(desc[2], PIM.Contact.FORMATTED_NAME);
  desc[2].class.getField("I.dataType.I").set(desc[2], PIM.PIMItem.STRING);
  desc[2].class.getField("I.maxValues.I").set(desc[2], -1);
});

Native.create("com/sun/j2me/pim/PIMProxy.getAttributesCount0.(I[I)I", function(listHandle, dataHandle) {
  console.warn("PIMProxy.getAttributesCount0.(I[I)I not implemented");
  return 1;
});

Native.create("com/sun/j2me/pim/PIMProxy.getAttributes0.(I[Lcom/sun/j2me/pim/PIMAttribute;I)V", function(listHandle, attr, dataHandle) {
  console.warn("PIMProxy.getAttributes0.(I[Lcom/sun/j2me/pim/PIMAttribute;I)V not implemented");
});
