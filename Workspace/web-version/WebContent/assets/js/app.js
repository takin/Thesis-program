'use strict';

angular.module('myThesis',['ngAnimate','ngSanitize'])
.directive('spinner', function () {
	return {
		restrict: 'E',
		templateUrl: 'loader.html'
	}
})
.directive('questionForm', function () {
	return {
		restrict: 'E',
		templateUrl: 'form.html'
	}
})
.directive('answerCard', function () {
	return {
		restrict: 'E',
		templateUrl: 'answer.html'
	}
})
.controller('appController', function ($http, $scope) {
	var _self = this;
	_self.serverAPI = 'api/ask?q=';
	_self.imagePattern = new RegExp("^(http://).*.\.(jpe?g|gif|png|svg)$", "i");
	_self.webURLPattern = new RegExp("^(https?).*\.[^(jpe?g|gif|png|svg)]+$", "i");

	$scope.loading = false;
	$scope.formIsSubmitted = false;
	$scope.dataIsReady = false;
	$scope.mainAnswer = '';
	$scope.facts = [];

	$scope.search = function () {
		$scope.facts = [];
		$scope.mainAnswer = '';
		$scope.dataIsReady = false;
		$scope.loading = true;

		if ( $scope.loading ) {
			$http.get( _self.serverAPI + $scope.q)
			.then((res) => {
				if ( res.data.code === 200) {
					
					var data = res.data.answer;

					$scope.mainAnswer = res.data.answer.text;
					$scope.facts = [];
					
					data.inferedFacts.forEach(function (value) {
						if( value !== null ) {
							var fact = {
								about: value.about,
								data:[]
							};

							var tempContentImage = [];
							
							for (var key in value.data ) {
								if ( value.data.hasOwnProperty(key) ) {
									var dataItem = {
										isSpan	: false,
										name 	: key,
										value 	: value.data[key]
									};

									if ( value.data[key].match(_self.imagePattern) ) {
										tempContentImage.push('<img src="' + value.data[key] + '" />');
									} else {
										if ( key.match(new RegExp("(comment|description)$","i")) ) {
											dataItem.isSpan = true;
											dataItem.value = '<span>' + value.data[key] + '</span>';
											fact.data.unshift(dataItem);
										} else {
											if ( value.data[key].match(_self.webURLPattern) ) {
												dataItem.value = '<a target="_blank" href="' + value.data[key] + '">' + value.data[key] +'</a>';
											}
											fact.data.push(dataItem);
										}
									}
								}
							}
							if ( tempContentImage.length > 0 ) {
								var x = fact.data[0];
								x.isSpan = true;
								var newContent = '';
								tempContentImage.forEach( function (item) {
									newContent += item + '<br/>';
								});
								x.value = newContent + x.value;
							}

							$scope.facts.push(fact);
						}
					});

				} else {
					$scope.mainAnswer = res.data.message;
				}

				$scope.loading = false;
				$scope.dataIsReady = true;
				// $scope.q = '';
			});
		}
	}
});