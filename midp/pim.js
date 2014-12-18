/* -*- Mode: Java; tab-width: 2; indent-tabs-mode: nil; c-basic-offset: 2 -*- */
/* vim: set shiftwidth=2 tabstop=2 autoindent cindent expandtab: */

'use strict';

var PIM = {};
PIM.CONTACT_LIST = 1;
PIM.EVENT_LIST = 2;
PIM.TODO_LIST = 3;
PIM.READ_ONLY = 1;
PIM.WRITE_ONLY = 2;
PIM.READ_WRITE = 3;

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

PIM.supportedFields = [
{
  field: PIM.Contact.FORMATTED_NAME,
  dataType: PIM.PIMItem.STRING,
  maxValues: -1,
},
{
  field: PIM.Contact.TEL,
  dataType: PIM.PIMItem.STRING,
  maxValues: -1,
},
{
  field: PIM.Contact.UID,
  dataType: PIM.PIMItem.STRING,
  maxValues: 1,
},
];

PIM.lastListHandle = 0;
PIM.openLists = {};

Native["com/sun/j2me/pim/PIMProxy.getListNamesCount0.(I)I"] = function(listType) {
  console.warn("PIMProxy.getListNamesCount0.(I)I incomplete");

  if (listType === PIM.CONTACT_LIST) {
    return 1;
  }

  return 0;
};

Native["com/sun/j2me/pim/PIMProxy.getListNames0.([Ljava/lang/String;)V"] = function(names) {
  console.warn("PIMProxy.getListNames0.([Ljava/lang/String;)V incomplete");
  names[0] = J2ME.newString("ContactList");
};

Native["com/sun/j2me/pim/PIMProxy.listOpen0.(ILjava/lang/String;I)I"] = function(listType, listName, mode) {
  console.warn("PIMProxy.listOpen0.(ILjava/lang/String;I)I incomplete");

  if (mode !== PIM.READ_ONLY) {
    console.warn("PIMProxy.listOpen0.(ILjava/lang/String;I)I in write mode not implemented");
    return 0;
  }

  if (listType === PIM.CONTACT_LIST) {
    PIM.openLists[++PIM.lastListHandle] = {};
    return PIM.lastListHandle;
  }

  return 0;
};

Native["com/sun/j2me/pim/PIMProxy.getNextItemDescription0.(I[I)Z"] = function(listHandle, description) {
  console.warn("PIMProxy.getNextItemDescription0.(I[I)Z incomplete");

  asyncImpl("Z", new Promise(function(resolve, reject) {
    contacts.getNext(function(contact) {
      if (contact == null) {
        resolve(0);
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

        resolve(1);
      });
    });
  }));
};

Native["com/sun/j2me/pim/PIMProxy.getNextItemData0.(I[BI)Z"] = function(itemHandle, data, dataHandle) {
  console.warn("PIMProxy.getNextItemData0.(I[BI)Z incomplete");
  data.set(PIM.curVcard);
  return 1;
};

Native["com/sun/j2me/pim/PIMProxy.getItemCategories0.(II)Ljava/lang/String;"] = function(itemHandle, dataHandle) {
  console.warn("PIMProxy.getItemCategories0.(II)Ljava/lang/String; not implemented");
  return null;
};

Native["com/sun/j2me/pim/PIMProxy.listClose0.(I)Z"] = function(listHandle, description) {
  if (!(listHandle in PIM.openLists)) {
    return 0;
  }

  delete PIM.openLists[listHandle];
  return 1;
};

Native["com/sun/j2me/pim/PIMProxy.getDefaultListName.(I)Ljava/lang/String;"] = function(listType) {
  if (listType === PIM.CONTACT_LIST) {
    return $S("ContactList");
  }

  if (listType === PIM.EVENT_LIST) {
    return $S("EventList");
  }

  if (listType === PIM.TODO_LIST) {
    return $S("TodoList");
  }

  return null;
};

Native["com/sun/j2me/pim/PIMProxy.getFieldsCount0.(I[I)I"] = function(listHandle, dataHandle) {
  return PIM.supportedFields.length;
};

Native["com/sun/j2me/pim/PIMProxy.getFieldLabelsCount0.(III)I"] = function(listHandle, fieldIndex, dataHandle) {
  console.warn("PIMProxy.getFieldLabelsCount0.(III)I not implemented");
  return 1;
};

Native["com/sun/j2me/pim/PIMProxy.getFields0.(I[Lcom/sun/j2me/pim/PIMFieldDescriptor;I)V"] = function(listHandle, desc, dataHandle) {
  console.warn("PIMProxy.getFields0.(I[Lcom/sun/j2me/pim/PIMFieldDescriptor;I)V incomplete");

  PIM.supportedFields.forEach(function(field, i) {
    desc[i].klass.classInfo.getField("I.field.I").set(desc[i], field.field);
    desc[i].klass.classInfo.getField("I.dataType.I").set(desc[i], field.dataType);
    desc[i].klass.classInfo.getField("I.maxValues.I").set(desc[i], field.maxValues);
  });
};

Native["com/sun/j2me/pim/PIMProxy.getAttributesCount0.(I[I)I"] = function(listHandle, dataHandle) {
  console.warn("PIMProxy.getAttributesCount0.(I[I)I not implemented");
  return 0;
};

Native["com/sun/j2me/pim/PIMProxy.getAttributes0.(I[Lcom/sun/j2me/pim/PIMAttribute;I)V"] = function(listHandle, attr, dataHandle) {
  console.warn("PIMProxy.getAttributes0.(I[Lcom/sun/j2me/pim/PIMAttribute;I)V not implemented");
};
