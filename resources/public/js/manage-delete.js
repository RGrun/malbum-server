function deleteImages() {

    var selectedInputs = $("input:checked");     // grab all the checked checkboxes
    var selectedIds = [];

    selectedInputs.each(function() {

         selectedIds.push($(this).attr('id')); // push them to the selectedIds array

    });

    if (selectedIds.length < 1)
        alert("Please select a photo to delete.");
    else
        // POST Ajax call
        $.post("/delete", // target URL
        {names: selectedIds},       // POST variables
        function(response) {        // callback function

            $.each(response, function() {
                if("ok" === this.status) {
                    var element = document.getElementById(this.name);
                    $(element).parent().parent().remove();
                    $("#manage-success-wrapper").css("display", "block");
                    $("#manage-success").append(
                        $('<li>', {html: "Successfully deleted photos."}));
                    console.log("Successfully deleted images.");
                } else {
                     $("#manage-errors-wrapper").css("display",  "block");
                     $("#manage-errors").append($('<li>', {html: "Failed to remove " + this.name + ": " + this.status}));
                     console.log("Failure: " + this.status);
                }

            });

        },
    "json");  // request result as JSON?
}

// executed on page load
$(document).ready(function(){
    $("#delete").click(deleteImages); // add onclick listener to delete button
});