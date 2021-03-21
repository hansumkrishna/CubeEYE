<?php
include "../active/active.php";
?>
<!DOCTYPE html>
<!--
	Dimension by HTML5 UP
	html5up.net | @ajlkn
	Free for personal and commercial use under the CCA 3.0 license (html5up.net/license)
-->
<html>

<head>
  <title>E | Y | E | C | U | B | E - Food</title>
  <meta charset="utf-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1, user-scalable=no" />
  <link rel="stylesheet" href="assets/css/main.css" />
  <noscript>
    <link rel="stylesheet" href="assets/css/noscript.css" />
  </noscript>
</head>

<body class="is-preload">
  <!-- Wrapper -->
  <div id="wrapper">
    <!-- Header -->
    <header id="header">
      <div class="logo">
        <span class="icon fa-cookie"></span>
      </div>
      <div class="content">
        <div class="inner">
          <h1>HELLO <?= $unme ?></h1>
          <p>
            What do you wanna eat?
          </p>
        </div>
      </div>
      <nav>
        <ul>
          <li><a href="../main/">Return Home</a></li>
          <li><a href="#ml">Meal</a></li>
          <li><a href="#qb">Quick Bites</a></li>
          <li><a href="#fb">Feedback</a></li>
        </ul>
      </nav>
    </header>

    <!-- Main -->
    <div id="main">
      <!-- Intro -->
      <article id="ml">
        <h2 class="major">Order Meals</h2>
        <p>
        <div class="table-wrapper">
          <table class="alt">
            <form method="post" action="./orderM.php" style="">
              <thead>
                <tr>
                  <th>Name</th>
                  <th>Price</th>
                  <th colspan="2">Order (Enter quantity)</th>
                </tr>
              </thead>
              <tbody>
                <tr>
                  <td><input type="hidden" value="Idly" />Idly</td>
                  <td>25</td>
                  <td><input type="text" id="one" name="lone" placeholder="0" /></td>
                </tr>
                <tr>
                  <td><input type="hidden" value="Idly + Vada" />Idly + Vada</td>
                  <td>35</td>
                  <td><input type="text" id="two" name="ltwo" placeholder="0" /></td>
                </tr>
                <tr>
                  <td><input type="hidden" value="Vada" />Vada</td>
                  <td>18</td>
                  <td><input type="text" id="three" name="lthree" placeholder="0" /></td>
                </tr>
                <tr>
                  <td><input type="hidden" value="Reg. Dosa" />VadaReg. Dosa</td>
                  <td>32</td>
                  <td><input type="text" id="four" name="lfour" placeholder="0" /></td>
                </tr>
                <tr>
                  <td><input type="hidden" value="Spl. Dosa" />Spl. Dosa</td>
                  <td>50</td>
                  <td><input type="text" id="five" name="lfive" placeholder="0" /></td>
                </tr>
              </tbody>
              <tfoot>
                <tr>
                  <td><button type="button" onclick="calc()">Calculate Total</button></td>
                  <td><button type="submit" name="submit" onclick="change(this.id)">Pay</button></td>
                  <td><input id="total" name="tot" style="color:black;" readonly value="Rs. 0" /></td>
            </form>
            </tr>
            </tfoot>
          </table>
        </div>
        </p>
      </article>

      <!-- Work -->
      <article id="qb">
        <h2 class="major">Order Quick Bites</h2>
        <p>
        <div class="table-wrapper">
          <table class="alt">
            <form method="post" action="./orderQ.php" style="">
              <thead>
                <tr>
                  <th>Name</th>
                  <th>Price</th>
                  <th colspan="2">Order (Enter quantity)</th>
                </tr>
              </thead>
              <tbody>
                <tr>
                  <td><input type="hidden" />Veg. Burger</td>
                  <td>25</td>
                  <td><input type="text" id="qone" name="bone" placeholder="0" value="" /></td>
                </tr>
                <tr>
                  <td><input type="hidden" />French Fries</td>
                  <td>35</td>
                  <td><input type="text" id="qtwo" name="btwo" placeholder="0" value="" /></td>
                </tr>
                <tr>
                  <td><input type="hidden" />Veg. Surprise Pastry</td>
                  <td>18</td>
                  <td><input type="text" id="qthree" name="bthree" placeholder="0" value="" /></td>
                </tr>
              </tbody>
              <tfoot>
                <tr>
                  <td><button type="button" onclick="calcq()">Calculate Total</button></td>
                  <td><button type="submit" name="submit" onclick="changeq(this.id)">Pay</button></td>
                  <td><input id="qtotal" name="qtot" style="color:black;" readonly value="Rs. 0" /></td>
            </form>
            </tr>
            </tfoot>
          </table>
        </div>
        </p>
      </article>

      <!-- Contact -->
      <article id="fb">
        <h2 class="major">Feedback</h2>
        <form method="post" action="./fback.php">
          <div class="fields">
            <div class="field half">
              <label for="name">Name</label>
              <input type="text" name="name" id="name" required />
            </div>
            <div class="field half">
              <label for="email">Email</label>
              <input type="text" name="email" id="email" required />
            </div>
            <div class="field">
              <label for="message">Message</label>
              <textarea name="message" id="message" rows="4" placeholder="Optional"></textarea>
            </div>
            <div class="field">
              <label for="message">How was your experience?</label>
              <input type="radio" id="good" name="val" />
              <label for="good">Good</label>
              <input type="radio" id="fine" name="val" />
              <label for="fine">Fine</label>
            </div>
          </div>
          <ul class="actions">
            <li>
              <input type="submit" name="submit" value="Send Feedback" class="primary" />
            </li>
            <li><input type="reset" value="Reset" /></li>
          </ul>
        </form>
        <ul class="icons">
          <li>
            <a href="#" class="icon brands fa-twitter"><span class="label">Twitter</span></a>
          </li>
          <li>
            <a href="#" class="icon brands fa-facebook-f"><span class="label">Facebook</span></a>
          </li>
          <li>
            <a href="#" class="icon brands fa-instagram"><span class="label">Instagram</span></a>
          </li>
          <li>
            <a href="#" class="icon brands fa-github"><span class="label">GitHub</span></a>
          </li>
        </ul>
      </article>
    </div>

    <!-- Footer -->
    <footer id="footer">
      <p class="copyright">
        &copy; Untitled. Design: <a href="https://html5up.net">HTML5 UP</a>.
      </p>
    </footer>
  </div>

  <!-- BG -->
  <div id="bg"></div>
  <script>
    var vone = document.getElementById("one");
    var vtwo = document.getElementById("two");
    var vthree = document.getElementById("three");
    var vfour = document.getElementById("four");
    var vfive = document.getElementById("five");
    var total = document.getElementById("total");

    function isE(a, e) {
      if (!a.value) {
        a.value = 0;
      } else {
        a.value = a.value * e;
      }
    }

    function change(a) {
      alert('PAID!');
    }

    function calc() {
      isE(vone, 25);
      isE(vtwo, 35);
      isE(vthree, 18);
      isE(vfour, 32);
      isE(vfive, 50);
      var tbill = eval(vone.value +
        '+' + vtwo.value + '+' + vthree.value +
        '+' + vfour.value + '+' + vfive.value);
      total.value = 'Rs. ' + tbill;
    }
  </script>
  <script>
    var qone = document.getElementById("qone");
    var qtwo = document.getElementById("qtwo");
    var qthree = document.getElementById("qthree");
    var qtotal = document.getElementById("qtotal");

    function isEq(a, e) {
      if (!a.value) {
        a.value = 0;
      } else {
        a.value = a.value * e;
      }
    }

    function defEq(a) {
      a.value = null;
      a.placeholder = 0;
    }

    function changeq(a) {
      alert('PAID!');
    }

    function calcq() {
      isEq(qone, 25);
      isEq(qtwo, 35);
      isEq(qthree, 18);
      var qtbill = eval(qone.value +
        '+' + qtwo.value + '+' + qthree.value);
      qtotal.value = 'Rs. ' + qtbill;
    }
  </script>


  <!-- Scripts -->
  <script src="assets/js/jquery.min.js"></script>
  <script src="assets/js/browser.min.js"></script>
  <script src="assets/js/breakpoints.min.js"></script>
  <script src="assets/js/util.js"></script>
  <script src="assets/js/main.js"></script>
</body>

</html>