document.addEventListener("DOMContentLoaded", function() {
		  var example1 = document.getElementById('example1');  
		  var hot = new Handsontable(example1, {
			data: sdata,
			stretchH: 'all',
			colHeaders: true,	
			rowHeaders: true,
			autoWrapRow: true,
			manualRowResize: true,
			manualColumnResize: true,
			nestedHeaders: scolsheader,
			columns: scolumns,
			cells: 
					function (row, col, prop) {
						var cellProperties = {};
						cellProperties.renderer = ValueRenderer1;
						return cellProperties;
					},
			contextMenu: true,
			minRows: 15,
			maxRows : 15,
			minSpareRows: 2,
			dropdownMenu: true,
			filters: true,
			columnSummary: [
			  {
				destinationRow: 10,
				destinationColumn: 9,
				type: 'sum',
				forceNumeric: true
			  },
			  {
				destinationRow: 10,
				destinationColumn: 4,
				type: 'sum',
				forceNumeric: true
			  }
			]
		  });
		  
			var sum1 = 0;
			var sum2 = 0;
			function ValueRenderer1(instance, td, row, col, prop, value, cellProperties) {
					Handsontable.TextCell.renderer.apply(this, arguments);
					//td.style.textAlign = 'center';
					if (!value || value === '') {
						//td.style.background = '#EEE';
					}
					 td.style.align = 'right';
					
					if (col === 4) {
						var n = instance.getDataAtCol(7).length;
						if (row < n - 1) {
							var a, b;
							a = instance.getDataAtCell(row, 1);
							b = instance.getDataAtCell(row, 3);
							if (a && b && !isNaN(a * b)) {
								td.innerHTML = Math.round(a * b);
								sum1 += parseFloat(td.innerHTML);
								td.style.fontStyle = 'italic';
								td.style.color = '#777';
								td.style.background = 'yellow';
							}
						}
					}
					if (col === 9) {
						var n = instance.getDataAtCol(7).length;
						if (row < n - 1) {
							var a, b;
							a = instance.getDataAtCell(row, 6);
							b = instance.getDataAtCell(row, 8);
							if (a && b && !isNaN(a * b)) {
								td.innerHTML = Math.round(a * b);
								sum1 += parseFloat(td.innerHTML);
								td.style.fontStyle = 'italic';
								td.style.color = '#777';
								td.style.background = 'yellow';
							}
						}
					}
				}
		  function bindDumpButton() {
			  if (typeof Handsontable === "undefined") {
				return;
			  }
		  
			  Handsontable.Dom.addEvent(document.body, 'click', function (e) {  
				var element = e.target || e.srcElement;
				if (element.nodeName == "BUTTON" && element.name == 'dump') {
				  var name = element.getAttribute('data-dump');
				  var instance = element.getAttribute('data-instance');
				  var hot = window[instance];
				  console.log('data of ' + name, hot.getData());
				}
			  });
			}
		  bindDumpButton();
		});