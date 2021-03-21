<?php
if (isset($_POST['submit'])) {
    function get_data()
    {
        $datae = array();
        $datae[] = array(
            'vb' => $_POST['bone'],
            'ff' => $_POST['btwo'],
            'vsp' => $_POST['bthree'],
            'total' => $_POST['qtot']
        );
        return json_encode($datae);
    }

    if (file_put_contents(
        "foodOrderQ.json",
        get_data()
    )) {
        header("refresh:3;url=/food/");
        echo 'ORDER SUCCESS!';
    } else {
        echo 'There is some error';
    }
}
