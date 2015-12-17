function selectUser() {

    var selected_user = $("#user-select").val();


    $.post("/photos-for-user",
            {userId : selected_user},
            function (response) {

                $("#table-body").html(""); // clear table body on each new response

                $.each(response, function() {

                    var newTableData = "";

                    if(this.custom_name == null) {
                        this.custom_name = this.name;
                    }

                    newTableData += "<tr id='" + this.name + "'>";
                    newTableData += "<td><img class='photo-management-image' src='/img/" + this.uname + "/" + this.thumb_name + "' /></td>";
                    newTableData += "<td>" + this.custom_name + "</td>";
                    newTableData += "<td>" + this.name + "</td>";
                    newTableData += "<td>" + this.upload_date + "</td>";
                    newTableData += "<td><input type='checkbox' id='" + this.name + "' name='" + this.name + "' /></td>";
                    newTableData += "</tr>";

                    $("#table-body").append(newTableData);

                });


            },
            "json");

}

function deleteImages() {

    var selectedInputs = $("input:checked");     // grab all the checked checkboxes
    var selectedIds = [];

    selectedInputs.each(function() {

         selectedIds.push($(this).attr('id')); // push them to the selectedIds array

    });

    if (selectedIds.length < 1)
        alert("Please select a photo to delete.");
    else
        var idOfUserToDelete = $("#user-select").val();

        // POST Ajax call
        $.post("/management-delete", // target URL
        {names: selectedIds, userid: idOfUserToDelete },       // POST variables
        function(response) {        // callback function

            $.each(response, function() {
                if("ok" === this.status) {
                    var element = document.getElementById(this.name);
                    $(element).remove();
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
    $("#user-select").change(selectUser); // add onchange listener to select list
    $("#delete").click(deleteImages); // add onclick listener to delete button
});