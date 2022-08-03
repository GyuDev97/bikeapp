//나의 기록 클릭시
$(".li_rr").click(function(){
	$("#recom_info").hide();
	drawPolylineByRR_Num(polyline, this.id);
	$("#courseInfo").show();
	$("#bt_like").hide();
	//rr_num을 이용하여 라이딩상세 기록 조회
	var mode = "ridingrecord";
	getRidingRecordByRR_Num(this.id, mode);
});

//스크랩코스 클릭시 
$(".li_sc").click(function(){
	$("#recom_info").hide();
	drawPolylineByRR_Num(polyline, this.id);
	$("#courseInfo").show();
	$("#bt_like").show();
	//rr_num을 이용하여 라이딩상세 기록 조회
	var mode = "scrap";
	getRidingRecordByRR_Num(this.id, mode);
});

 // 코스명 변경 시
 function changeRR_Name(){
	 if($("#rr_name").val()==null || $("#rr_name").val() =="" || $("#rr_name").val() ==" "){
		 alert("코스 명을 입력한 후 버튼을 눌러주세요.");
		 return;
	 }
	 $.ajax({
			url : "/ridingrecord/rr_name",
			type : "put",
			data : {
				rr_num : $("#selected_rr_num").val(),
				rr_name : $("#rr_name").val()
			},
			dataType : "json",
			cache : false, 
			complete : function(data) {
				alert('코스 명을 변경하였습니다.');
			}
	});
 }
 
 function recom(r_num){
	console.log(r_num);
	
	$.ajax({
		url:"/info/riding/recom/"+r_num,
		type:"get",
		success:function(data){
			console.log(data);
			
			var level;
			if(data.percent>80){
				level = "하";
			}else if(data.percent>60){
				level = "중하";
			}else if(data.percent>40){
				level = "중";
			}else if(data.percent>20){
				level = "중상";
			}else{
				level = "상";
			}
			
			
			
			$("#modal_cname").text( data.rs_name);
			$("#modal_distance").text( parseInt(data.rs_distance)/1000 +'km');
			$("#modal_high").text( data.rs_high+'m');
			$("#modal_avgspeed").text( data.rs_avgspeed+'km/h');
			$("#modal_cnt").text( data.rs_cnt+'회');
			$("#modal_area").text( data.rs_area);
			$("#modal_level").text( level+' ['+data.truerank+'/'+data.total+']' );
		}
	});
	
	$('#dialog-message').dialog({
		modal: true, 
		buttons: {
				"스크랩하기": function() { $.ajax({
				url : "/riding/scrap/"+r_num+"/"+userId,
				type : "post",
				cache : false,
				complete : function(data){
					if(data.responseText=="success"){
						alert('해당 코스를 스크랩하였습니다!');
					}else if(data.responseText=="fail"){
						alert('이미 스크랩한 코스입니다!');
					}else if(data.responseText=="failMyRR"){
						alert('나의 주행기록입니다...');
					}
				}
			}); },
			"닫기": function() { $(this).dialog('close'); }
		}
	});
	
		
 }
 
 $(document).ready(function(){
			
	for(var i=0;i<3;i++){
		var mapContainer = document.getElementById('rmap'+$(".recom_num")[i].value), // 지도를 표시할 div 
	    mapOption = { 
	        center: new kakao.maps.LatLng(33.450701, 126.570667), // 지도의 중심좌표
	        level: 3 // 지도의 확대 레벨
	    };

		// 지도를 표시할 div와  지도 옵션으로  지도를 생성합니다
		var map = new kakao.maps.Map(mapContainer, mapOption);
		
		$.ajax({
			 url : "/getGpxByRR_Num",
			 type : "GET",
			 data : {rr_num : $(".recom_num")[i].value},
			 dataType : "json",
			 async : false,
			 cache : false,
			 success : function(data) {
					
					var point = data.infos;
					var linePath = [];
					
					// 지도를 재설정할 범위정보를 가지고 있을 LatLngBounds 객체를 생성합니다
					var bounds = new kakao.maps.LatLngBounds();
			
					for(var i=0;i<point.length;i++){
						var location = new kakao.maps.LatLng(point[i].lat, point[i].lon);
						linePath.push(location);
						bounds.extend(location);
					}
					
					// 지도에 표시할 선을 생성합니다
					var temp_polyline = new kakao.maps.Polyline({
					    path: linePath, // 선을 구성하는 좌표배열 입니다
					    strokeWeight: 10, // 선의 두께 입니다
					    strokeColor: '#00FF7F', // 선의 색깔입니다
					    strokeOpacity: 0.5, // 선의 불투명도 입니다 1에서 0 사이의 값이며 0에 가까울수록 투명합니다
					    strokeStyle: 'solid' // 선의 스타일입니다
					});
					
					// 지도에 표시할 선을 생성합니다
					var temp_polyline2 = new kakao.maps.Polyline({
					    path: linePath, // 선을 구성하는 좌표배열 입니다
					    strokeWeight: 5, // 선의 두께 입니다
					    strokeColor: '#8B008B', // 선의 색깔입니다
					    strokeOpacity: 0.7, // 선의 불투명도 입니다 1에서 0 사이의 값이며 0에 가까울수록 투명합니다
					    strokeStyle: 'solid' // 선의 스타일입니다
					});
					
					// 지도에 선을 표시합니다 
					temp_polyline.setPath(linePath);
					// 지도에 선을 표시합니다 
					temp_polyline.setMap(map);
					// 지도에 선을 표시합니다 
					temp_polyline2.setPath(linePath);
					// 지도에 선을 표시합니다 
					temp_polyline2.setMap(map);
					// 맵을 이동시킵니다.
					map.setBounds(bounds);
			}
		});
	}
});