/**
 * Created by tkagnus on 19/03/2017.
 */

// jQuery Plugin Boilerplate

(function ($) {
    $.newDatacenter = function (element, options) {
        var defaults = {};
        var plugin = this;
        plugin.settings = {};

        var $element = $(element), // reference to the jQuery version of DOM element
            element = element;    // reference to the actual DOM element
        plugin.init = function () {

            plugin.$modal = $element.closest('.modal');
            plugin.data = {
                pes: []
            };

            $element.click(function (e) {
                e.preventDefault();
                createDatacenter();
            });

            plugin.$peModal = plugin.$modal.find('.pe-modal');

            plugin.$peModal.find('#savePe').click(function (e) {
                e.preventDefault();

                var tmp = {};

                plugin.$peModal.find('input').each(function () {
                    var $this = $(this);
                    console.log($this);
                    var value = $this.val();
                    var key = $this.attr('name');

                    tmp[key] = value;
                });
                plugin.data.pes.push(tmp);
            });

            //todo close !


        };

        var createDatacenter = function () {

            plugin.$modal.find('input').each(function (e, v) {
                var $this = $(this);
                var value = $this.val();
                var key = $this.attr('name');

                plugin.data[key] = value;

            });

            createDatacenterInstance();

        };

        var createDatacenterInstance = function () {

            var $template = $('[data-datacenter-id="template"]').clone();

            $template.attr('data-datacenter-params', plugin.data);

            var id = ($('.datacenter-container').length) - 1;

            $template.attr('data-datacenter-id', id);

            $('.datacenters-containter').append($template);
        };

        plugin.init();

    };

    $.fn.newDatacenter = function (options) {
        return this.each(function () {
            var $this = $(this);
            if (undefined == $this.data('newDatacenter')) {
                var plugin = new $.newDatacenter(this, options);
                $this.data('newDatacenter', plugin);
            }

        });
    };

})(jQuery);


$('#saveDatacenter').newDatacenter();
