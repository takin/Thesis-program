'use strict';

angular.module('myThesis',['ngAnimate','ngSanitize'])
.directive('spinner', () => {
	return {
		restrict: 'E',
		templateUrl: 'loader.html'
	}
})
.directive('questionForm', () => {
	return {
		restrict: 'E',
		templateUrl: 'form.html'
	}
})
.directive('answerCard', () => {
	return {
		restrict: 'E',
		templateUrl: 'answer.html'
	}
})
.controller('appController', ($scope, $http) => {
	$scope.loading = false;
	$scope.formIsSubmitted = false;
	$scope.dataIsReady = false;
	$scope.answer = {};

	$scope.search = () => {
		$scope.dataIsReady = false;
		$scope.loading = true;
		if ( $scope.loading ) {
			$http.get('http://localhost:8080/web-version/api/qa?q=' + $scope.q)
			.then((res) => {
				
				if ( res.data.code === 200 ) {
					
					var data = res.data.answer;

					$scope.mainAnswer = res.data.answer.text;
					$scope.facts = [];
					
					data.inferedFacts.forEach((value) => {
						var fact = {
							about: value.about,
							data:[]
						};
						
						for (var key in value.data ) {
							if ( value.data.hasOwnProperty(key) ) {

								var dataItem = {};
								dataItem.name = key;
								dataItem.value = value.data[key];

								var imagePattern = new RegExp("\.(jpe?g|gif|png)$", "i");
								var webURLPattern = new RegExp("^(https?).*\.[^(jpe?g|gif|png)]+$", "i");

								if ( value.data[key].match(imagePattern) ) {
									dataItem.value = '<img src="' + value.data[key] + '" />';
								}

								if ( value.data[key].match(webURLPattern) ) {
									dataItem.value = '<a target="_blank" href="' + value.data[key] + '">' + value.data[key] +'</a>';
								}

								fact.data.push(dataItem);
							}
						}

						$scope.facts.push(fact);
					});

				} else {
					$scope.mainAnswer = res.data.message;
				}

				$scope.loading = false;
				$scope.dataIsReady = true;
			});
		}
	}
});