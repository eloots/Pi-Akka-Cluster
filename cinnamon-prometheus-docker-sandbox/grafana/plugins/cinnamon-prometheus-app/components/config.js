'use strict';

System.register(['lodash'], function (_export, _context) {
    "use strict";

    var _, _createClass, CinnamonPrometheusAppConfigCtrl;

    function _classCallCheck(instance, Constructor) {
        if (!(instance instanceof Constructor)) {
            throw new TypeError("Cannot call a class as a function");
        }
    }

    return {
        setters: [function (_lodash) {
            _ = _lodash.default;
        }],
        execute: function () {
            _createClass = function () {
                function defineProperties(target, props) {
                    for (var i = 0; i < props.length; i++) {
                        var descriptor = props[i];
                        descriptor.enumerable = descriptor.enumerable || false;
                        descriptor.configurable = true;
                        if ("value" in descriptor) descriptor.writable = true;
                        Object.defineProperty(target, descriptor.key, descriptor);
                    }
                }

                return function (Constructor, protoProps, staticProps) {
                    if (protoProps) defineProperties(Constructor.prototype, protoProps);
                    if (staticProps) defineProperties(Constructor, staticProps);
                    return Constructor;
                };
            }();

            _export('CinnamonPrometheusAppConfigCtrl', CinnamonPrometheusAppConfigCtrl = function () {
                function CinnamonPrometheusAppConfigCtrl(backendSrv) {
                    var _this = this;

                    _classCallCheck(this, CinnamonPrometheusAppConfigCtrl);

                    this.backendSrv = backendSrv;
                    this.datasourceName = 'Cinnamon Prometheus';
                    this.isDatasourceCreated = false;
                    this.appEditCtrl.setPreUpdateHook(this.preUpdate.bind(this));

                    this.appModel.jsonData = this.appModel.jsonData || {};
                    this.appModel.jsonData.prometheusUrl = this.appModel.jsonData.prometheusUrl || "http://prometheus:9090";
                    this.validation = { prometheusUrlValid: true };

                    backendSrv.get('/public/plugins/cinnamon-prometheus-app/plugin.json').then(function (data) {
                        _this.dashboards = _.filter(data.includes, { type: 'dashboard' });
                    });
                }

                _createClass(CinnamonPrometheusAppConfigCtrl, [{
                    key: 'preUpdate',
                    value: function preUpdate() {
                        if (this.isDatasourceCreated) {
                            return Promise.resolve();
                        } else {
                            return this.createDatasource();
                        }
                    }
                }, {
                    key: 'init',
                    value: function init() {
                        var _this2 = this;

                        this.backendSrv.get('/api/datasources').then(function (datasources) {
                            _this2.isDatasourceCreated = _.findIndex(datasources, { name: _this2.datasourceName }) !== -1;
                        });
                    }
                }, {
                    key: 'createDatasource',
                    value: function createDatasource() {

                        this.validation.prometheusUrlValid = !this.isEmptyOrUndefined(this.appModel.jsonData.prometheusUrl);

                        if (_.values(this.validation).some(function (value) {
                            return !value;
                        })) {
                            return Promise.reject();
                        } else {
                            return this.backendSrv.post('/api/datasources', {
                                "name": this.datasourceName,
                                "type": "prometheus",
                                "url": this.appModel.jsonData.prometheusUrl,
                                "access": "proxy",
                                "isDefault": false
                            });
                        }
                    }
                }, {
                    key: 'isEmptyOrUndefined',
                    value: function isEmptyOrUndefined(value) {
                        return value === undefined || value == null || value.trim() === "";
                    }
                }]);

                return CinnamonPrometheusAppConfigCtrl;
            }());

            _export('CinnamonPrometheusAppConfigCtrl', CinnamonPrometheusAppConfigCtrl);

            CinnamonPrometheusAppConfigCtrl.templateUrl = 'components/config.html';
        }
    };
});
