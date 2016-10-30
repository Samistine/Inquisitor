
        <div id="footer" class="grid_16">
            <p>
                Page generated: ${now?string("MMM dd, yyyy HH:mm:ss z")}
            </p>
            <p>
                Powered by <a href="https://samistine.com/" target="_blank">Samistine's</a> <a href="https://www.spigotmc.org/resources/inquistor-refresh.5821/" target="_blank">${pluginName} v${pluginVersion}</a> Original Authors: frdfsnlght, snarfattack
            </p>
            <p>
                Fugue Icons &copy; 2012 Yusuke Kamiyamane. All rights reserved. Icons are available under a <a href="https://creativecommons.org/licenses/by/3.0/">Creative Commons Attribution 3.0 License</a>.
            </p>
            <p>
                Other icons and images &copy; 2012 Mojang. All rights reserved.
            </p>
            <p>
                <a href="https://github.com/Samistine/Inquisitor/" target="_blank"><img src="../img/logo.png"/></a>
            </p>
        </div>
        <div class="clear"></div>

        </div> <!-- container -->
        </div> <!-- background -->

        <script type="text/javascript">
            $(document).ready(function(){

                $("#playerSearchInput").autocomplete({
                    minLength: 1,
                    autoFocus: true,
                    delay: 250,
                    select: function(event, ui) {
                        event.target.value = ui.item.value;
                        event.target.form.submit();
                    },
                    source: function(request, response) {
                        $.getJSON('../api/findPlayers', { playerName: request.term }, function(data) {
                            if (! data.success) return;
                            var names = data.result;
                            if (names.length > 20) names = names.slice(0, 20);
                            response(names);
                        });
                    }
                })
                .data('autocomplete')._renderItem = function(ul, item) {
                    var val = $("#playerSearchInput").val();
                    var re = new RegExp(val, 'i');
                    return $('<li></li>')
                        .data('item.autocomplete', item)
                        .append('<a>' + item.label.replace(re, '<em>' + val + '</em>') + '</a>')
                        .appendTo(ul);
                };

            });
        </script>

    </body>
</html>
