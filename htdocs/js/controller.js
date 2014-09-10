'use strict';

/**
 * Created by kai on 09.09.14.
 */

var connaisseurControllers = angular.module('connaisseurControllers', []);

connaisseurControllers.controller('NavCtrl', function ($scope, $location) {
    $scope.isActive = function(menue) {
        var route = $location.path().split("/")[1];
        return menue === route;
    }
});

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

connaisseurControllers.controller('UserDetailCtrl', function ($scope, $http, $routeParams) {
    $http.get('/user/' + $routeParams.userId).success(function(data) {
        $scope.user = data.response;
    });
});
