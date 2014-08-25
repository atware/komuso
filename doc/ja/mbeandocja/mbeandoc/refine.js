var cache = new Array();
function refine(target,str){
  var elements = cache[target];
  if(!elements){
    elements = cache[target] = getElementsByName4IE(target);
  }
  var search = str.toLowerCase();
  for(var i=0;i<elements.length;i++){
    if(-1 != elements[i].id.toLowerCase().indexOf(search)){
      elements[i].style.display = "block";
    }else{
      elements[i].style.display = "none";
    }
  }
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
