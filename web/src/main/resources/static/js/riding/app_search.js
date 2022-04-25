$(document).ready(function() {
	loadCourse();
	
});

var location_markers = []; //장소검색시 사용할 마커배열

// 장소 검색 객체를 생성합니다
var ps = new kakao.maps.services.Places();

// 검색 결과 목록이나 마커를 클릭했을 때 장소명을 표출할 인포윈도우를 생성합니다
var infowindow = new kakao.maps.InfoWindow({zIndex:1});

//장소 검색하기를 눌렀을 때
$("#btn_search_course").click(function(){
	if($("#search_loation").val()=="" || $("#search_loation").val()==" " || $("#search_loation").val()==null ){
		alert("장소를 입력한 후 검색하기 버튼을 눌러주세요.");
		return;
	}
	$("#form_search").submit();
});

// 키워드 검색을 요청하는 함수입니다
function appsearchPlaces() {
		alert('search아님');
	var option = $("#searchCondition option:selected").val();
	var keyword = document.getElementById('keyword').value;
	var size = 5;
	
	if(option==0){
		

	    if (!keyword.replace(/^\s+|\s+$/g, '')) {
	        alert('키워드를 입력해주세요!');
	        return false;
	    }
	
	    // 장소검색 객체를 통해 키워드로 장소검색을 요청합니다
	    ps.keywordSearch( keyword, placesSearchCB, size);
	} else if( option == 1){
		var pagenum;
		if( $("#pageNum").val() == 1 || $("#pageNum").val() == null ){
			pagenum = 1;
		}else {
			pagenum = $("#pageNum").val();
		}
		$.ajax({
			url : "/info/riding/search/"+pagenum+"/"+keyword,
			type : "get",
			dataType : "json",
			cache : false,
			success : function(data){
				var listEl = document.getElementById('placesList'); 
    	
    			//검색 결과 목록에 추가된 항목들을 제거합니다
    			removeAllChildNods(listEl);
			    // 지도에 표시되고 있는 마커를 제거합니다
			    removeMarker();
			    
				for(var i=0;i<data.length;i++){
					var str = "<tr><td class='item' onclick='clickCouseName(this)' id='"+data[i].rr_num+"'><span class='markerbg marker_"+i+"'></span>"
					+"<div class='info'><h5>"+data[i].rr_name+"</h5>"
					+"<span>거리 : "+ data[i].rr_distance +"m</span>"
					+"<span>추천 수 : "+ data[i].rr_like+"개</span></div></td></tr>";
					
					$("#placesList").append(str);
					
					if(i==0){
						var paginationEl = document.getElementById('pagination');
					
					    // 기존에 추가된 페이지번호를 삭제합니다
					    while (paginationEl.hasChildNodes()) {
					        paginationEl.removeChild (paginationEl.lastChild);
					    }
					
						for(var j=data[i].rr_time; j<=data[i].rr_breaktime; j++){
							var str2 = "<a href='#' class='on' onclick='clickPageNum("+j+")'>"+j+"</a>";
							$("#pagination").append(str2);
						}
					}
				}
			}
		})
	}
}

//장소 검색 시 등록된 코스들 로드
function loadCourse(){
	$.ajax({
		url : "/riding/course/list",
		type : "get",
		dataType : "json",
		cache : false,
		success : function(data) {
			for(var i=0;i<data.length;i++){
				$.ajax({
					url : "/getGpxByRR_Num",
					type : "GET",
					data : {rr_num : data[i].rr_num},
					dataType : "json",
					cache : false,
					success : function(data2) {
						var point = data2.infos;
						var linePath = [];
						var rr_num = data2.rr_num;
						for(var k=0;k<point.length;k++){
							var location = new kakao.maps.LatLng(point[k].lat, point[k].lon);
							linePath.push(location);
						}
						// 지도에 표시할 선을 생성합니다
						var polyline = new kakao.maps.Polyline({
						    path: linePath, // 선을 구성하는 좌표배열 입니다
						    strokeWeight: 7, // 선 의 두께 입니다
						    strokeColor: "#FF6600", // 선의 색깔입니다
						    strokeOpacity: 0.5, // 선의 불투명도 입니다 1에서 0 사이의 값이며 0에 가까울수록 투명합니다
						    strokeStyle: 'solid' // 선의 스타일입니다
						});
						var mode = 'course';
						kakao.maps.event.addListener(polyline, 'click', function() {
							$("#box_courseInfo").show();
							selectedPolyline.setPath(linePath);
							selectedPolyline.setMap(map);
							getRidingRecordByRR_Num(rr_num, mode);
					    });
						// 지도에 선을 표시합니다 
						polyline.setMap(map);
					}
				});
			}
		}
	});
}


// 장소검색이 완료됐을 때 호출되는 콜백함수 입니다
var sizee=5;
function placesSearchCB(data, status, pagination, sizee) {
    if (status === kakao.maps.services.Status.OK) {
    	
        // 정상적으로 검색이 완료됐으면
        // 검색 목록과 마커를 표출합니다
        displayPlaces(data);
        
        // 페이지 번호를 표출합니다
        displayPagination(pagination);
        
    } else if (status === kakao.maps.services.Status.ZERO_RESULT) {
        alert('검색 결과가 존재하지 않습니다.');
        return;
    } else if (status === kakao.maps.services.Status.ERROR) {
        alert('검색 결과 중 오류가 발생했습니다.');
        return;
    }
}

// 검색 결과 목록과 마커를 표출하는 함수입니다
function displayPlaces(places) {

    var listEl = document.getElementById('placesList'), 
    menuEl = document.getElementById('box_search_course'),
    fragment = document.createDocumentFragment(),
    bounds = new kakao.maps.LatLngBounds(),
    listStr = '';
    
    // 검색 결과 목록에 추가된 항목들을 제거합니다
    removeAllChildNods(listEl);

    // 지도에 표시되고 있는 마커를 제거합니다
    removeMarker();
    
    for ( var i=0; i<places.length; i++ ) {

        // 마커를 생성하고 지도에 표시합니다
        var placePosition = new kakao.maps.LatLng(places[i].y, places[i].x),
            marker = addMarker(placePosition, i), 
            itemEl = getListItem(i, places[i]); // 검색 결과 항목 Element를 생성합니다

		// 검색된 장소 위치를 기준으로 지도 범위를 재설정하기위해
		// LatLngBounds 객체에 좌표를 추가합니다
		bounds.extend(placePosition);
            
        // 마커와 검색결과 항목에 mouseover 했을때
        // 해당 장소에 인포윈도우에 장소명을 표시합니다
        // mouseout 했을 때는 인포윈도우를 닫습니다
        (function(marker, title) {
            kakao.maps.event.addListener(marker, 'mouseover', function() {
                displayInfowindow(marker, title);
            });

            kakao.maps.event.addListener(marker, 'mouseout', function() {
                infowindow.close();
            });

            itemEl.onmouseover =  function () {
                displayInfowindow(marker, title);
            };

            itemEl.onmouseout =  function () {
                infowindow.close();
            };
        })(marker, places[i].place_name);

        fragment.appendChild(itemEl);
    }

    // 검색결과 항목들을 검색결과 목록 Elemnet에 추가합니다
    listEl.appendChild(fragment);
    menuEl.scrollTop = 0;

    // 검색된 장소 위치를 기준으로 지도 범위를 재설정합니다
	map.setBounds(bounds);	
    map.panTo(new kakao.maps.LatLng(places[0].y, places[0].x));
}

// 검색결과 항목을 Element로 반환하는 함수입니다
function getListItem(index, places) {
    var el = document.createElement('tr'),
   
    
    itemStr = '<td><span class="markerbg marker_' + (index+1) + '"></span>' +
                
                ' ' + places.place_name + '</td>';

    if (places.road_address_name) {
        itemStr += '    <td><span>' + places.road_address_name + '</span>' +
                    '   <span class="jibun gray">' +  places.address_name  + '</span></td>';
    } else {
        itemStr += '    <span>' +  places.address_name  + '</span>'; 
    }
                 
      itemStr +='</div></tr>';           

    el.innerHTML = itemStr;
    el.className = 'item';

    return el;
}

// 마커를 생성하고 지도 위에 마커를 표시하는 함수입니다
function addMarker(position, idx, title) {
    var imageSrc = 'https://t1.daumcdn.net/localimg/localimages/07/mapapidoc/marker_number_blue.png', // 마커 이미지 url, 스프라이트 이미지를 씁니다
        imageSize = new kakao.maps.Size(36, 37),  // 마커 이미지의 크기
        imgOptions =  {
            spriteSize : new kakao.maps.Size(36, 691), // 스프라이트 이미지의 크기
            spriteOrigin : new kakao.maps.Point(0, (idx*46)+10), // 스프라이트 이미지 중 사용할 영역의 좌상단 좌표
            offset: new kakao.maps.Point(13, 37) // 마커 좌표에 일치시킬 이미지 내에서의 좌표
        },
        markerImage = new kakao.maps.MarkerImage(imageSrc, imageSize, imgOptions),
            marker = new kakao.maps.Marker({
            position: position, // 마커의 위치
            image: markerImage 
        });

    marker.setMap(map); // 지도 위에 마커를 표출합니다
    location_markers.push(marker);  // 배열에 생성된 마커를 추가합니다

    return marker;
}

// 지도 위에 표시되고 있는 마커를 모두 제거합니다
function removeMarker() {
    for ( var i = 0; i < location_markers.length; i++ ) {
    	location_markers[i].setMap(null);
    }   
    location_markers = [];
}

// 검색결과 목록 하단에 페이지번호를 표시는 함수입니다
function displayPagination(pagination) {
	
    var paginationEl = document.getElementById('pagination'),
        fragment = document.createDocumentFragment(),
        i; 

    // 기존에 추가된 페이지번호를 삭제합니다
    while (paginationEl.hasChildNodes()) {
        paginationEl.removeChild (paginationEl.lastChild);
    }

    for (i=1; i<=pagination.last; i++) {
        var el = document.createElement('a');
        el.href = "#";
        el.innerHTML = i;

        if (i===pagination.current) {
            el.className = 'on';
        } else {
            el.onclick = (function(i) {
                return function() {
                    pagination.gotoPage(i);
                }
            })(i);
        }

        fragment.appendChild(el);
    }
    paginationEl.appendChild(fragment);
}

// 검색결과 목록 또는 마커를 클릭했을 때 호출되는 함수입니다
// 인포윈도우에 장소명을 표시합니다
function displayInfowindow(marker, title) {
    var content = '<div style="padding:5px;z-index:1;">' + title + '</div>';

    infowindow.setContent(content);
    infowindow.open(map, marker);
}

 // 검색결과 목록의 자식 Element를 제거하는 함수입니다
function removeAllChildNods(el) {   
    while (el.hasChildNodes()) {
        el.removeChild (el.lastChild);
    }
}

function clickPageNum(pageNum){
	$("#pageNum").val(pageNum);
	searchPlaces();
}

function clickCouseName(e){
	$.ajax({
		url : "/getGpxByRR_Num",
		type : "GET",
		data : {rr_num : e.id},
		dataType : "json",
		cache : false,
		success : function(data2) {
			var point = data2.infos;
			var linePath = [];
			var rr_num = data2.rr_num;
			loadHashTag(rr_num);
			
			// 지도를 재설정할 범위정보를 가지고 있을 LatLngBounds 객체를 생성합니다
			var tempbounds = new kakao.maps.LatLngBounds();
			
			for(var k=0;k<point.length;k++){
				var location = new kakao.maps.LatLng(point[k].lat, point[k].lon);
				linePath.push(location);
				tempbounds.extend(location);
			}
			
			var mode = 'course';
			$("#box_courseInfo").show();
			selectedPolyline.setPath(linePath);
			selectedPolyline.setMap(map);
			getRidingRecordByRR_Num(rr_num, mode);
			map.setBounds(tempbounds);
		}
	
	});
}