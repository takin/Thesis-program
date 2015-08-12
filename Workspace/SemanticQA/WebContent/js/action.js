'use strict';

angular.module('semanticQA',['ngAnimate'])
.config(function($locationProvider){
	$locationProvider.html5Mode(true);
})
.directive('spinner', function(){
	return {
		restrict: 'C',
		templateUrl:'loader.html'
	}
})
.directive('answer', function(){
	return {
		templateUrl:'answer.html'
	}
})
.controller('appController', ['$rootScope','$scope','$location', '$http',function($rootScope, $scope, $location, $http){

	// flag untuk menandakan proses search belum dilakukan 
	// flag ini berguna untuk menentukan apakah loader harus dimunculkan atau tidak
	$scope.searchPerformed = false;
	
	// flag ini untuk mentrigger blok answer untuk ditampilkan atau tidak
	$scope.hasResponse = false;

	$scope.search = function(){
		// append query param ke url
		$location.search('question', $scope.question);
		
		// clear the input value
		$scope.question = '';
	}
	
	// monitor perubahan pada URL
	$rootScope.$on('$locationChangeSuccess', function(){
		
		var theQuestion = $location.search().question;
		
		// jika terjadi perubahan pada URL dan query param tidak kosong
		// maka lakukan proses http request untuk mencari jawaban
		if(typeof theQuestion !== 'undefined'){
		
			$http.get('api/qa?q=' + theQuestion)
				.then(function(res){
					
					// flag search perform to false so the loader gone
					$scope.searchPerformed = false;
					
					// flag this to true so the anwer is displayed
					$scope.hasResponse = true;
					
					$scope.result = res.data;					
				});
		}
	});
}]);