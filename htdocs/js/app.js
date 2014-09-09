'use strict';

/**
 * Created by kai on 09.09.14.
 */

var connaisseur = angular.module('connaisseur', ['ngRoute', 'connaisseurControllers']);

connaisseur.config(['$routeProvider',
    function($routeProvider) {
        $routeProvider.
            when('/movies', {
                templateUrl: 'partials/movie-search.html',
                controller: 'MovieSearchCtrl'
            }).
            when('/movies/:movieId', {
                templateUrl: 'partials/movie-detail.html',
                controller: 'MovieDetailCtrl'
            }).
            otherwise({
                redirectTo: '/movies'
            });
    }]);