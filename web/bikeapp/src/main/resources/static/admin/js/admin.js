(function () {
  var body = document.body;
  var sidebarToggle = document.querySelector('.sidebar-toggle');
  if (sidebarToggle) {
    sidebarToggle.addEventListener('click', function () { body.classList.toggle('sidebar-open'); });
  }

  var mapElement = document.getElementById('hazard-map');
  if (mapElement) initializeMap(mapElement);

  var form = document.getElementById('hazard-edit-form');
  if (form) {
    form.querySelectorAll('[data-action]').forEach(function (button) {
      button.addEventListener('click', function () { handleAction(form, button.dataset.action); });
    });
  }

  function initializeMap(element) {
    var key = body.dataset.kakaoKey;
    var latitude = parseFloat(element.dataset.lat);
    var longitude = parseFloat(element.dataset.lng);
    if (!key || !isFinite(latitude) || !isFinite(longitude)) return;

    window.__trailgoInitMap = function () {
      window.kakao.maps.load(function () {
        var position = new window.kakao.maps.LatLng(latitude, longitude);
        var map = new window.kakao.maps.Map(element, { center: position, level: 5 });
        var markerData = Array.from(element.querySelectorAll('.hazard-marker-data'));
        if (!markerData.length) markerData = [{ dataset: { lat: latitude, lng: longitude, title: '위험지역', type: 'OTHER' } }];
        markerData.forEach(function (item) {
          var markerPosition = new window.kakao.maps.LatLng(Number(item.dataset.lat), Number(item.dataset.lng));
          var markerClass = String(item.dataset.type || 'OTHER').toLowerCase().replace(/_/g, '-');
          var content = document.createElement('div');
          content.className = 'kakao-hazard-marker type-' + markerClass;
          content.title = item.dataset.title || '위험지역';
          content.innerHTML = '<span>!</span>';
          new window.kakao.maps.CustomOverlay({ map: map, position: markerPosition, content: content, yAnchor: 1 });
        });
      });
    };

    if (window.kakao && window.kakao.maps) {
      window.__trailgoInitMap();
      return;
    }
    var script = document.createElement('script');
    script.src = 'https://dapi.kakao.com/v2/maps/sdk.js?autoload=false&appkey=' + encodeURIComponent(key);
    script.onload = window.__trailgoInitMap;
    document.head.appendChild(script);
  }

  function handleAction(form, action) {
    var id = form.dataset.hazardId;
    if (action === 'approve') {
      if (window.confirm('이 위험지역을 승인할까요?')) request('/admin/api/hazards/' + id + '/approve', 'POST', {});
      return;
    }
    if (action === 'reject') {
      var reason = window.prompt('반려 사유를 입력해주세요.');
      if (reason !== null) request('/admin/api/hazards/' + id + '/reject', 'POST', { reason: reason });
      return;
    }
    if (action === 'merge') {
      var targetId = window.prompt('병합할 대상 위험지역 번호를 입력해주세요.');
      if (targetId && /^\d+$/.test(targetId)) request('/admin/api/hazards/' + id + '/merge', 'POST', { targetId: Number(targetId) });
      return;
    }
    if (action === 'save') {
      var payload = {};
      new FormData(form).forEach(function (value, key) { payload[key] = value; });
      request('/admin/api/hazards/' + id, 'PATCH', payload);
    }
  }

  function request(url, method, payload) {
    var token = document.querySelector('meta[name="_csrf"]');
    var header = document.querySelector('meta[name="_csrf_header"]');
    var headers = { 'Content-Type': 'application/json' };
    if (token && header) headers[header.content] = token.content;

    fetch(url, { method: method, headers: headers, body: JSON.stringify(payload) })
      .then(function (response) {
        return response.json().then(function (data) {
          if (!response.ok) throw new Error(data.message || '요청을 처리하지 못했습니다.');
          return data;
        });
      })
      .then(function () { window.location.reload(); })
      .catch(function (error) { window.alert(error.message); });
  }
}());
