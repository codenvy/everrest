var clientIFrame;
var _facade;
 

xda = {
 create :function (facade){
  _facade=facade;
  clientIFrame = document.createElement("iframe");
    var stateId =0;
    var frameUrl = facade.clientURI + "#0:init:id=" + stateId + "&server=" + encodeURIComponent(facade.serverURI);
		clientIFrame.setAttribute("src",frameUrl);
		clientIFrame.setAttribute("width","400");
		clientIFrame.setAttribute("height","400");
    clientIFrame.setAttribute("id","clientFrame_" + stateId);
		clientIFrame.setAttribute("style" , "position: absolute; top: 0px; left: 0px; width: 1px; height: 1px; visibility: hidden;");
    document.getElementById("body").appendChild(clientIFrame);
},

 send :function (id){
    var client = document.getElementById("clientFrame_" + id).contentWindow;
	var reqHeaders = [];
	for (var param in _facade.requestHeaders) {
		reqHeaders.push(param + ": " + _facade.requestHeaders[param]);
	}
	var requestData = "uri=" + encodeURIComponent(_facade.apiURI);
	if (reqHeaders.length > 0) {
		requestData += "&requestHeaders=" + encodeURIComponent(reqHeaders.join("\r\n"));
	}
	if (_facade.method) {
		requestData += "&method=" + encodeURIComponent(_facade.method);
	}
	if (_facade.data) {
		requestData += "&data=" + encodeURIComponent(_facade.data);
	}
	client.send(requestData);
   
},
receive:function (stateId, urlEncodedData) {
	var response = {};
	var nvPairs = urlEncodedData.split("&");
	for (var i = 0; i < nvPairs.length; i++) {
		if (nvPairs[i]) {
			var nameValue = nvPairs[i].split("=");
			response[decodeURIComponent(nameValue[0])] = decodeURIComponent(nameValue[1]);
		}
	}
	
	_facade.setResponseHeaders(response.responseHeaders);
	
	if (response.status == 0 || response.status) {
		_facade.status = parseInt(response.status, 10);
	}
	if (response.statusText) {
		_facade.statusText = response.statusText;
	}
	if (response.responseText) {
		_facade.responseText = response.responseText;
		var _parent = clientIFrame.parentNode;
		_parent.removeChild(clientIFrame);
	}
	_facade.readyState = 4;
	_facade.load(_facade);
	
 }
}

xdaInit = function () {
	var facade = {};	
	facade.requestHeaders = {};
	facade.allResponseHeaders = null;
	facade.responseHeaders = {};
	facade.method = "POST";
	facade.uri = null;
	facade.data = null;
	facade.responseText = null;
	facade.status = null;
	facade.statusText = null;
	facade.readyState = 0;
	facade.serverURI = null;
	facade.clientURI = null;
	facade.apiURI = null;
	facade.stateId = null;
	facade.load = null;
	facade.setRequestHeader = function (header, value) {
	  facade.requestHeaders[header] = value;
  },
	facade.getAllResponseHeaders = function(){
		return facade.allResponseHeaders;
	}
  facade.getResponseHeader = function(headerName){
		return facade.responseHeaders[headerName];
	}
	facade.setResponseHeaders =function (allHeaders) {
	if (allHeaders) {
		this.allResponseHeaders = allHeaders;
		allHeaders = allHeaders.replace(/\r/g, "");
		var nvPairs = allHeaders.split("\n");
		for (var i = 0; i < nvPairs.length; i++) {
			if (nvPairs[i]) {
				var nameValue = nvPairs[i].split(": ");
				this.responseHeaders[nameValue[0]] = nameValue[1];
			}
		}
	}
 }
	return facade;
};


