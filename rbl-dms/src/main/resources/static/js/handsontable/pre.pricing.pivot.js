function dopricing(a){
function c(a,c,d,e,f,g,h){
	if(Handsontable.TextCell.renderer.apply(this,arguments),
			c.style.align="right",
			5===e)
	{
		var i=a.getDataAtCol(8).length;if(d<i-1){
			var j,k;
			j=a.getDataAtCell(d,2),
			k=a.getDataAtCell(d,4),
			j&&k&&!isNaN(j*k)&&(c.innerHTML=j*k,
					b+=parseFloat(c.innerHTML),
					c.style.fontStyle="italic",
					c.style.color="#777",
					c.style.background="yellow")
					}
	}
	if(10===e){
		var i=a.getDataAtCol(8).length;
		if(d<i-1){
			var j,k;
			j=a.getDataAtCell(d,7),
			k=a.getDataAtCell(d,9),
			j&&k&&!isNaN(j*k)&&(c.innerHTML=j*k,
					b+=parseFloat(c.innerHTML),
					c.style.fontStyle="italic",
					c.style.color="#777",
					c.style.background="yellow")
					}
		}
	}
function d(){
	"undefined"!=typeof Handsontable&&(Handsontable.Dom.addEvent(document.body,"click",function(a){
		var b=a.target||a.srcElement;
		if("BUTTON"==b.nodeName&&"dump"==b.name){
			var c=b.getAttribute("data-dump"),
			d=b.getAttribute("data-instance"),
			e=window[d];console.log("data of "+c,e.getData())			
		}		
	}),
	Handsontable.Dom.addEvent(save,"click",function(){
		alert(JSON.stringify({data:a.getData()}))
		}
	)
	)}
var a=document.getElementById(a),
a=new Handsontable(a,{
	data:sdata,
	colWidths:140,
	colHeaders:!0,
	minRows:50,
	rowHeaders:!0,
	autoWrapRow:!0,
	manualRowResize:!0,
	manualColumnResize:!0,
	nestedHeaders:[
	               ["CONTRACT","CN/423627",{label:"CS=0.45 PS=SS=test",colspan:3},
	                "CONTRACT","CN/423627",{label:"CS=0.45 PS=SS=test",colspan:3}],
	               ["Contract","LHS Lot Code","Cts","Pcs","Rate","Amount","PD/063976","Cts","Pcs","Rate","Amount"]
	              ],
	columns:[
	         {type:"text"},
	         {type:"text"},
	         {type:"numeric",className:"htRight",format:"00.00"},
	         {type:"numeric",className:"htRight",format:"00.00"},
	         {type:"numeric",className:"htRight",format:"00.00"},
	         {type:"numeric",className:"htRight",format:"00.00"},
	         {type:"text"},
	         {type:"numeric",className:"htRight",format:"00.00"},
	         {type:"numeric",className:"htRight",format:"00.00"},
	         {type:"numeric",className:"htRight",format:"00.00"},
	         {type:"numeric",className:"htRight",format:"00.00"}],
	cells:function(a,b,d){
	var e={};
	return e.renderer=c,e},
	contextMenu:!0,
	minSpareRows:1,
	dropdownMenu:!0,
	filters:!0
	}),
	b=0;
	d()
}