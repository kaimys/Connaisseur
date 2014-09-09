'use strict';

/**
 * Created by kai on 09.09.14.
 */

var connaisseurControllers = angular.module('connaisseurControllers', []);

connaisseurControllers.controller('MovieSearchCtrl', function ($scope, $http) {
    $scope.search = function(query) {
        $http.get('/itemSearch/' + $scope.query).success(function(data) {
            $scope.movies = data.response.searchResult;
            $("#search-result").fadeIn("slow");
        });
    }
});

connaisseurControllers.controller('MovieDetailCtrl', function ($scope, $http, $routeParams) {
    $http.get('/items/' + $routeParams.movieId).success(function(data) {
        $scope.movies = data.response;
    });
});

