package com.chenenyu.router;

import com.chenenyu.router.RouteRequest;
import com.chenenyu.router.RouteResponse;

interface IRouterInterface {
    RouteResponse route(in RouteRequest request);
}
