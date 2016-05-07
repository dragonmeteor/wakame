    var callback = function() {};  
    
    $(function() {
        $( "#accordion" ).accordion({
          heightStyle: "content",
          active: false,
          collapsible: true,
            activate: function() { callback(); }
        });
      });
      
        $(".buttons a").click(function(event) {
            var active = $("#accordion").accordion("option", "active")+"";
            if(active != "0") {
                event.preventDefault();
                var ahref = $(this).attr("href");
                callback = function() {
                    location.href = ahref;
                    callback = function() { };
                };
                $("#accordion").accordion("option", "active", 0);
            }
        });    var callback = function() {};  
    
    $(function() {
        $( "#accordion" ).accordion({
          heightStyle: "content",
          active: false,
          collapsible: true,
            activate: function() { callback(); }
        });
      });
      
        $(".buttons a").click(function(event) {
            var active = $("#accordion").accordion("option", "active")+"";
            if(active != "0") {
                event.preventDefault();
                var ahref = $(this).attr("href");
                callback = function() {
                    location.href = ahref;
                    callback = function() { };
                };
                $("#accordion").accordion("option", "active", 0);
            }
        });
