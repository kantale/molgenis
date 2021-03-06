(function($, molgenis) {
	"use strict";
	
	var ns = molgenis;
	var pagination = new ns.Pagination();
	var restApi = new ns.RestClient();
	var searchApi = new ns.SearchClient();
	var selectedDataSet = null;
	var sortRule = null;
	
	ns.CatalogueChooser = function OntologyAnnotator(){
		
	};
	
	ns.CatalogueChooser.prototype.changeDataSet = function(selectedDataSet){
		if(selectedDataSet !== null && selectedDataSet !== '' && selectedDataSet !== undefined){
			var dataSetEntity = restApi.get('/api/v1/dataset/' + selectedDataSet);
			$('#selected-catalogue').empty().append(dataSetEntity.Name);
			var request = {
				documentType : 'protocolTree-' + ns.hrefToId(dataSetEntity.href),
				query:{
					rules:[[{
						field : 'type',
						operator : 'EQUALS',
						value : 'observablefeature'
					}]]
				}
			};
			searchApi.search(request, function(searchResponse){
				sortRule = null;
				$('#catalogue-name').empty().append(dataSetEntity.Name);
				$('#dataitem-number').empty().append(searchResponse.totalHitCount);
				pagination.reset();
				ns.CatalogueChooser.prototype.updateSelectedDataset(selectedDataSet);
				ns.CatalogueChooser.prototype.createMatrixForDataItems();
				initSearchDataItems(dataSetEntity);
			});
		}else{
			$('#catalogue-name').empty().append('Nothing selected');
			$('#dataitem-number').empty().append('Nothing selected');
		}
		
		function initSearchDataItems (dataSet) {
			$('#search-dataitem').typeahead({
				source: function(query, process) {
					ns.CatalogueChooser.prototype.dataItemsTypeahead('observablefeature', ns.hrefToId(dataSet.href), query, process);
				},
				minLength : 3,
				items : 20
			}).on('keydown', function(e){
			    if (e.which == 13) {
			    	$('#search-button').click();
			    	return false;
			    }
			}).on('keyup', function(e){
				if($(this).val() === ''){
					ns.CatalogueChooser.prototype.createMatrixForDataItems();
			    }
			});
			$('#search-button').click(function(){
				ns.CatalogueChooser.prototype.createMatrixForDataItems();
			});
		}
	};
	
	ns.CatalogueChooser.prototype.createMatrixForDataItems = function() {
		var documentType = 'protocolTree-' + getSelectedDataSet();
		
		var q = {
				rules : [[{
					field : 'type',
					operator : 'SEARCH',
					value : 'observablefeature'
				}]]
		}
		
		var queryText = $('#search-dataitem').val();
		if(queryText !== ''){
			q.rules[0].push({
				operator : 'AND'
			});
			q.rules[0].push({
				operator : 'SEARCH',
				value : queryText
			});
			pagination.reset();
		}
		if(sortRule !== null)
		{
			q.sort = sortRule;
		}

		searchApi.search(pagination.createSearchRequest(documentType, q), function(searchResponse) {
			var searchHits = searchResponse.searchHits;
			var tableObject = $('#dataitem-table');
			var tableBody = $('<tbody />');
			
			$.each(searchHits, function(){
				$(createTableRow($(this)[0]["columnValueMap"])).appendTo(tableBody);
			});
			
			tableObject.empty().append(createTableHeader()).append(tableBody);
			pagination.setTotalPage(Math.ceil(searchResponse.totalHitCount / pagination.getPager()));
			pagination.updateMatrixPagination($('.pagination ul'), ns.CatalogueChooser.prototype.createMatrixForDataItems);
		});
		
		function createTableRow(feature){
			var row = $('<tr />');
			var description = feature.description;
			var isPopOver = description.length < 120;
			var descriptionSpan = $('<span />').html(isPopOver ? description : description.substring(0, 120) + '...');
			if(!isPopOver){
				descriptionSpan.addClass('show-popover');
				descriptionSpan.popover({
					content : description,
					trigger : 'hover',
					placement : 'bottom'
				});
			}
			
			var featureNameSpan = $('<span>' + feature.name + '</span>');
			$('<td />').append(featureNameSpan).appendTo(row);
			$('<td />').append(descriptionSpan).appendTo(row);
			return row;
		}
		
		function createTableHeader(){
			var headerRow = $('<tr />');
			var firstColumn = $('<th>Name</th>').css('width', '30%').appendTo(headerRow);
			if (sortRule) {
				if (sortRule.orders[0].direction == 'ASC') {
					$('<span data-value="Name" class="ui-icon ui-icon-triangle-1-s down float-right"></span>').appendTo(firstColumn);
				} else {
					$('<span data-value="Name" class="ui-icon ui-icon-triangle-1-n up float-right"></span>').appendTo(firstColumn);
				}
			} else {
				$('<span data-value="Name" class="ui-icon ui-icon-triangle-2-n-s updown float-right"></span>').appendTo(firstColumn);
			}
			$('<th>Description</th>').css('width', '70%').appendTo(headerRow);
			
			// Sort click
			$(firstColumn).find('.ui-icon').click(function() {
				if (sortRule && sortRule.orders[0].direction == 'ASC') {
					sortRule = {
							orders: [{
								property: 'name',
								direction: 'DESC'
							}]
					};
				} else {
					sortRule = {
							orders: [{
								property: 'name',
								direction: 'ASC'
							}]
					};
				}
				ns.CatalogueChooser.prototype.createMatrixForDataItems();
				return false;
			});
			
			return $('<thead />').append(headerRow);
		}
		
		function getSelectedDataSet(){
			return selectedDataSet;
		}
	};
	
	ns.CatalogueChooser.prototype.updateIndex = function(ontologyTermIRI, boost){
		var updateRequest = {
			'ontologyTermIRI' : ontologyTermIRI,
			'updateScript' : 'boost=' + boost
		};
		$.ajax({
			type : 'POST',
			url : molgenis.getContextUrl() + '/update',
			async : false,
			data : JSON.stringify(updateRequest),
			contentType : 'application/json',
		});
	}
	
	ns.CatalogueChooser.prototype.updateSelectedDataset = function(dataSet) {
		selectedDataSet = dataSet;
	};
	
	ns.CatalogueChooser.prototype.dataItemsTypeahead = function (type, dataSetId, query, response){
		var queryRules = [{
			field : 'type',
			operator : 'EQUALS',
			value : type,
		},{
			operator : 'AND'
		},{
			operator : 'SEARCH',
			value : query
		}];
		var searchRequest = {
			documentType : 'protocolTree-' + dataSetId,
			query :{
				pageSize: 20,
				rules: [queryRules]
			}
		};
		searchApi.search(searchRequest, function(searchReponse){
			var result = [];
			var dataMap = {};
			$.each(searchReponse.searchHits, function(index, hit){
				var value = hit.columnValueMap.ontologyTerm;
				if($.inArray(value, result) === -1){
					var name = hit.columnValueMap.name;
					result.push(name);
					dataMap[name] = hit.columnValueMap;
				}
			});
			$(document).data('dataMap', dataMap);
			response(result);
		});
	};
}($, window.top.molgenis = window.top.molgenis || {}));
