<?php
include "../active/active.php"
?>
<!DOCTYPE html>
<!--
	Aerial by HTML5 UP
	html5up.net | @ajlkn
	Free for personal and commercial use under the CCA 3.0 license (html5up.net/license)
-->
<html>

<head>
  <title>E | Y | E | C | U | B | E - Main</title>
  <meta charset="utf-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1, user-scalable=no" />
  <link rel="stylesheet" href="assets/css/main.css" />
  <noscript>
    <link rel="stylesheet" href="assets/css/noscript.css" />
  </noscript>
  <script src="https://cdnjs.cloudflare.com/ajax/libs/jquery/3.2.1/jquery.min.js"></script>
  <script src="./assets/index.js"></script>
</head>

<body class="is-preload">
  <div id="wrapper">
    <div id="bg"></div>
    <div id="overlay"></div>
    <div id="main">
      <!-- Header -->
      <header id="header">
        <h1><?php
            echo $unme;
            ?></h1>
        <p>How can we help you pit-stop today?</p>
        <nav>
          <ul>
            <li>
              <a href="../voice/index.html" class="icon fa-microphone"><span class="label">Voice</span></a>
            </li>
          </ul>
          <ul>
            <li>
              <a href="../food/" class="icon fa-cookie-bite"><span class="label">Food</span></a>
            </li>
            <li>
              <a href="../cser/" class="icon fa-car-crash"><span class="label">Car Service</span></a>
            </li>
            <li>
              <a href="../shop/" class="icon fa-shopping-cart"><span class="label">Shopping</span></a>
            </li>
          </ul>
          <ul>
            <li>
              <a href="../home/"><button style="height:50px;width:70px">Logout</button></span></a>
            </li>
          </ul>
        </nav>
      </header>

      <!-- Footer -->
      <footer id="footer">
        <span class="copyright">&copy; Untitled. Design:
          <a href="http://html5up.net">HTML5 UP</a>.</span>
      </footer>
    </div>
  </div>

  <script>
    window.onload = function() {
      document.body.classList.remove("is-preload");
    };
    window.ontouchmove = function() {
      return false;
    };
    window.onorientationchange = function() {
      document.body.scrollTop = 0;
    };
  </script>
</body>

</html>