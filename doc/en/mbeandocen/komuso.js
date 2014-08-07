function removeElement(array,index){
  array.splice(index,1);
}

function MBeanAttribute(label,attributeName,objectName){
  this.label = label;
  this.attributeName = attributeName;
  this.objectName = objectName;
}

var attributes = new Array();
function addMBeanAttribute(label,attributeName,objectName){
  attributes[attributes.length] = new MBeanAttribute(label,attributeName,objectName);
  refreshAttributeTable();
  constructSetting();
}
function removeMBeanAttribute(index){
  removeElement(attributes,index);
  refreshAttributeTable();
  constructSetting();
}
function refreshAttributeTable(){
  var html = '<table border="1" cellspacing="0" cellpadding="3"><tr><th nowrap></th><th nowrap>Label</th>'
  +'<th>Attribute</th><th>ObjectName</th></tr>';
  for(var i=0;i<attributes.length;i++){
    html +=
    '<tr><td><input type="button" name="remove" onclick="removeMBeanAttribute('+i+');constructSetting()" value="Remove"></td>'
+'<td nowrap><input style="width:100%" type="text" onkeyup="setLabel(this.value,'+i+')" value="'+attributes[i].label+'"></td>'
+'<td>'+attributes[i].attributeName+'</td>'
+'<td style="width:100%"><input style="width:100%" readonly="true" type="text" value="'+attributes[i].objectName+'"></td>'
+'</tr>';
  }
  html+="</table>";
  document.getElementById("attributeTable").innerHTML = html;
  document.getElementById("tab2title").innerHTML = "Preferences("+attributes.length+")";
}

function constructSetting(){
  var setting = "#MBean connection info\n";
  var setting = "#JMX Remote Protocol Provider\n";
  setting +="jmx.remote.protocol.provider.pkgs="
   + document.getElementById("jmx.remote.protocol.provider.pkgs").value+"\n";
  setting +="JMXServiceURL="
   + document.getElementById("JMXServiceURL").value+"\n";
  setting +="#User ID\n";
  setting +="java.naming.security.principal="
   + document.getElementById("java.naming.security.principal").value+"\n";
  setting +="#Password\n";
  setting +="java.naming.security.credentials="
   + document.getElementById("java.naming.security.credentials").value+"\n";
  setting +="#Sampling Interval(sec)\n";
  setting +="interval=" + document.getElementById("interval").value+"\n";
  setting +="#Number of times to sample(-1 for forever)\n";
  setting +="count=" + document.getElementById("count").value+"\n";
  setting +="#Timestamp format\n"
  setting +="dateFormat=" + document.getElementById("dateFormat").value+"\n";
  setting +="#logback Logger name for CSV output\n"
  setting +="csvLogger=" + document.getElementById("csvLogger").value+"\n";
  setting +="#logback Logger name for status output\n"
  setting +="statusLogger=" + document.getElementById("statusLogger").value+"\n";
  //set optionsl parameters
                                                                                                                setting +="#MBeans\n";
  setting +="mbeans=";
  var attrList = "";
  for(var i=0;i<attributes.length;i++){
    if(i != 0){
      setting +=",";
    }
    setting += native2ascii(attributes[i].label);
    attrList += native2ascii(attributes[i].label) + ".attributeName=" + native2ascii(attributes[i].attributeName)+"\n";
    attrList += native2ascii(attributes[i].label) + ".objectName=" + native2ascii(attributes[i].objectName)+"\n";
  }
  setting += "\n" + attrList;
  document.getElementById("setting").value = setting;
  document.getElementById("setting").rows = setting.split("\n").length+2;
}
function native2ascii(str){
  return unescape(escape(str).replace(/%(u[0-9a-fA-F]{4})/g, "\\$1")).split(" ").join("\\ ").split("=").join("\\=");
}

function setLabel(newLabel,index){
  attributes[index].label = newLabel;
  constructSetting();
}
function getElementsByName4IE(name,tagname) {
  if(tagname){
     var elem = document.getElementsByTagName(tagname);
     var arr = new Array();
     for(i = 0; i < elem.length; i++) {
          att = elem[i].getAttribute("name");
          if(att == name) {
               arr[arr.length] = elem[i];
          }
     }
     return arr;
  }else{
     var elem = document.getElementsByTagName("div");
     var arr = new Array();
     for(i = 0; i < elem.length; i++) {
          att = elem[i].getAttribute("name");
          if(att == name) {
               arr[arr.length] = elem[i];
          }
     }
     elem = document.getElementsByTagName("tr");
     for(i = 0,iarr = 0; i < elem.length; i++) {
          att = elem[i].getAttribute("name");
          if(att == name) {
               arr[arr.length] = elem[i];
          }
     }
     return arr;
  }
}
