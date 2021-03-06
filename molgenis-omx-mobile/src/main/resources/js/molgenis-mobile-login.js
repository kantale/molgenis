var MolgenisMobileConfig = {
	startPage : '#catalogue-page',
	featureCount : 25
};
 
$(document).bind("mobileinit", function() {
	var ns = window.top.molgenis;
	
	//Configure JQM to have a longer list before skipping transitions
	 $.mobile.getMaxScrollForTransition = function () { return 65536; } 

	//Show spinner and overlay when doing a ajax request
	$(document).on({
	    ajaxStart: function() { 
	    	$('body').mask();
			$.mobile.showPageLoadingMsg();
	    },
	    ajaxStop: function() { 
	    	$.mobile.hidePageLoadingMsg(); 
			$('body').unmask();
	    }    
	});
	
	$(document).on('pagebeforeshow', '#login-page', ns.onLoginPageBeforeShow);
	$(document).on('click', '.logout', ns.logout);
});

(function($, molgenis) {
	"use strict";
	var ns = molgenis;
	
	ns.isUserAuthenticated = function(callback) {
		$.ajax({
			url : '/mobile/authenticated',
			async:false,
			success : function(authenticated) {
				if (authenticated && $.isFunction(callback.authenticated)) {
					callback.authenticated();
				} else if (!authenticated && $.isFunction(callback.unAuthenticated)){
					callback.unAuthenticated();
				}
			}
		});
	}
	
	ns.onLoginPageBeforeShow = function() {
		$('#username').val('');
		$('#password').val('');
		
		//Don't submit the page but call out ajax login method
		$.validator.setDefaults({
			submitHandler: function() {
				ns.login();
			}
		});
		
		//Validate occurs on form submit
		$("#login-form").validate({
			rules: {
				username: "required",
				password: "required"
			},
			messages: {
				username: "Please enter your username",
				password: "Please enter your password",
			},
			errorPlacement: function(error, element) {
				error.insertAfter($(element).parent());	
			}
		});
	}
	
	ns.login = function() {
		$.ajax({
			type: $('#login-form').attr('method'),
			url: $('#login-form').attr('action'),
			data: $('#login-form').serialize(),
			async:false,
			success : function(response) {
				$.mobile.changePage(MolgenisMobileConfig.startPage, {transition: "flip"});
			},
			error: function(jqXHR, textStatus, errorThrown) {
				alert(errorThrown)
			}
		});
	}
	
	ns.logout = function() {
		$.ajax({
			type: 'POST',
			url: '/logout',
			success : function() {
				$('#features').html('').listview('refresh');
				$.mobile.changePage("#login-page", {transition: "flip", reverse: true});
			}
		});
	}
}($, window.top.molgenis = window.top.molgenis || {}));