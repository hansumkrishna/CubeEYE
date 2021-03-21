<?php
if (isset($_POST['submit'])) {
    function get_data()
    {
        $datae = array();
        $datae[] = array(
            'idly' => $_POST['lone'],
            'inv' => $_POST['ltwo'],
            'v' => $_POST['lthree'],
            'rd' => $_POST['lfour'],
            'sd' => $_POST['lfive'],
            'total' => $_POST['tot'],
        );
        return json_encode($datae);
    }

    if (file_put_contents(
        "foodOrderM.json",
        get_data()
    )) {
        header("refresh:3;url=/food/");
        echo 'ORDER SUCCESS!';
    } else {
        echo 'There is some error';
    }
}
