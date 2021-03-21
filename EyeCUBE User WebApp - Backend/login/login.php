<?php

if ($_SERVER['REQUEST_METHOD'] == 'POST') {
    $uemail = $_POST['uemail'];
    $str = file_get_contents('./loginData.json');
    $json = json_decode($str, true);
    foreach ($json as $user) {
        if ($user['email'] === $uemail) {
            $n = $user['name'];
            header("refresh:3;url=/entry/index.php?name=$n");
            echo "LOGIN OK";
            file_put_contents('./activeData.json', $user['name']);
            break;
        }
    }
}
