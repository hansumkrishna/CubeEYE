<?php
include "../active/active.php"
?>
<!DOCTYPE html>
<html lang="en">

<head>
  <meta charset="utf-8" />
  <meta http-equiv="X-UA-Compatible" content="IE=edge" />
  <!-- Tell the browser to be responsive to screen width -->
  <meta name="viewport" content="width=device-width, initial-scale=1" />
  <meta name="description" content="" />
  <meta name="author" content="" />
  <!-- Favicon icon -->
  <link rel="icon" type="image/png" sizes="16x16" href="images/favicon.png" />
  <title>Ela - Bootstrap Admin Dashboard Template</title>
  <!-- Bootstrap Core CSS -->
  <link href="css/lib/bootstrap/bootstrap.min.css" rel="stylesheet" />
  <!-- Custom CSS -->

  <link href="css/lib/calendar2/semantic.ui.min.css" rel="stylesheet" />
  <link href="css/lib/calendar2/pignose.calendar.min.css" rel="stylesheet" />
  <link href="css/lib/owl.carousel.min.css" rel="stylesheet" />
  <link href="css/lib/owl.theme.default.min.css" rel="stylesheet" />
  <link href="css/helper.css" rel="stylesheet" />
  <link href="css/style.css" rel="stylesheet" />
  <!-- HTML5 Shim and Respond.js IE8 support of HTML5 elements and media queries -->
  <!-- WARNING: Respond.js doesn't work if you view the page via file:** -->
  <!--[if lt IE 9]>
      <script src="https:**oss.maxcdn.com/libs/html5shiv/3.7.0/html5shiv.js"></script>
      <script src="https:**oss.maxcdn.com/libs/respond.js/1.4.2/respond.min.js"></script>
    <![endif]-->
</head>

<body class="fix-header fix-sidebar">
  <!-- Preloader - style you can find in spinners.css -->
  <div class="preloader">
    <svg class="circular" viewBox="25 25 50 50">
      <circle class="path" cx="50" cy="50" r="20" fill="none" stroke-width="2" stroke-miterlimit="10" />
    </svg>
  </div>
  <!-- Main wrapper  -->
  <div id="main-wrapper">
    <!-- header header  -->
    <div class="header">
      <nav class="navbar top-navbar navbar-expand-md navbar-light">
        <!-- Logo -->
        <div class="navbar-header">
          <a class="navbar-brand" href="index.html">
            <!-- Logo icon -->
            <b><img src="images/logo.png" alt="homepage" class="dark-logo" /></b>
            <!--End Logo icon -->
            <!-- Logo text -->
            <span><img src="images/logo-text.png" alt="homepage" class="dark-logo" /></span>
          </a>
        </div>
        <!-- End Logo -->
        <div class="navbar-collapse">
          <!-- toggle and nav items -->
          <ul class="navbar-nav mr-auto mt-md-0">
            <!-- This is  -->
            <li class="nav-item">
              <a class="nav-link nav-toggler hidden-md-up text-muted" href="javascript:void(0)"><i class="mdi mdi-menu"></i></a>
            </li>
            <li class="nav-item m-l-10">
              <a class="nav-link sidebartoggler hidden-sm-down text-muted" href="javascript:void(0)"><i class="ti-menu"></i></a>
            </li>
        </div>
      </nav>
    </div>
    <!-- End header header -->
    <!-- Left Sidebar  -->
    <div class="left-sidebar">
      <!-- Sidebar scroll-->
      <div class="scroll-sidebar">
        <!-- Sidebar navigation-->
        <nav class="sidebar-nav">
          <ul id="sidebarnav">
            <li class="nav-devider"></li>
            <li class="nav-label">Home</li>
          </ul>
        </nav>
        <!-- End Sidebar navigation -->
      </div>
      <!-- End Sidebar scroll-->
    </div>
    <!-- End Left Sidebar  -->
    <!-- Page wrapper  -->
    <div class="page-wrapper">
      <!-- Bread crumb -->
      <div class="row page-titles">
        <div class="col-md-5 align-self-center">
          <h3 class="text-primary">Dashboard</h3>
        </div>
        <div class="col-md-7 align-self-center">
          <ol class="breadcrumb">
            <li class="breadcrumb-item">
              <a href="javascript:void(0)">Home</a>
            </li>
            <li class="breadcrumb-item active">Dashboard</li>
          </ol>
        </div>
      </div>
      <!-- End Bread crumb -->
      <!-- Container fluid  -->
      <div class="container-fluid">
        <!-- Start Page Content -->
        <div class="row">
          <div class="col-md-3">
            <div class="card p-30">
              <div class="media">
                <div class="media-left meida media-middle">
                  <span><i class="fa fa-usd f-s-40 color-primary"></i></span>
                </div>
                <div class="media-body media-text-right">
                  <h2>43111</h2>
                  <p class="m-b-0">Total Revenue</p>
                </div>
              </div>
            </div>
          </div>
          <div class="col-md-3">
            <div class="card p-30">
              <div class="media">
                <div class="media-left meida media-middle">
                  <span><i class="fa fa-shopping-cart f-s-40 color-success"></i></span>
                </div>
                <div class="media-body media-text-right">
                  <h2>118</h2>
                  <p class="m-b-0">Sales</p>
                </div>
              </div>
            </div>
          </div>
          <div class="col-md-3">
            <div class="card p-30">
              <div class="media">
                <div class="media-left meida media-middle">
                  <span><i class="fa fa-archive f-s-40 color-warning"></i></span>
                </div>
                <div class="media-body media-text-right">
                  <h2>15</h2>
                  <p class="m-b-0">Stores</p>
                </div>
              </div>
            </div>
          </div>
          <div class="col-md-3">
            <div class="card p-30">
              <div class="media">
                <div class="media-left meida media-middle">
                  <span><i class="fa fa-user f-s-40 color-danger"></i></span>
                </div>
                <div class="media-body media-text-right">
                  <h2>147</h2>
                  <p class="m-b-0">Customer</p>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="row bg-white m-l-0 m-r-0 box-shadow">
          <!-- column -->
          <div class="col-lg-8">
            <div class="card">
              <div class="card-body">
                <h4 class="card-title">Statistics</h4>
                <div id="extra-area-chart"></div>
              </div>
            </div>
          </div>
          <!-- column -->

          <!-- column -->
          <div class="col-lg-4">
            <div class="card">
              <div class="card-body browser">
                <p class="f-w-600">
                  Idly <span class="pull-right">85%</span>
                </p>
                <div class="progress">
                  <div role="progressbar" style="width: 85%; height: 8px" class="progress-bar bg-danger wow animated progress-animated">
                    <span class="sr-only">60% Complete</span>
                  </div>
                </div>

                <p class="m-t-30 f-w-600">
                  Idly + Vada<span class="pull-right">90%</span>
                </p>
                <div class="progress">
                  <div role="progressbar" style="width: 90%; height: 8px" class="progress-bar bg-info wow animated progress-animated">
                    <span class="sr-only">60% Complete</span>
                  </div>
                </div>

                <p class="m-t-30 f-w-600">
                  T - Shirt<span class="pull-right">65%</span>
                </p>
                <div class="progress">
                  <div role="progressbar" style="width: 65%; height: 8px" class="progress-bar bg-success wow animated progress-animated">
                    <span class="sr-only">60% Complete</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
          <!-- column -->
        </div>
        <div class="row">
          <div class="col-lg-9">
            <div class="card">
              <div class="card-title">
                <h4>Recent Orders</h4>
              </div>
              <div class="card-body">
                <div class="table-responsive">
                  <table class="table">
                    <thead>
                      <tr>
                        <th>Name</th>
                        <th>Product</th>
                        <th>Quantity</th>
                        <th>Status</th>
                      </tr>
                    </thead>
                    <tbody>
                      <tr>

                        <td><?= $unme ?></td>
                        <td><span>Idly + Vada</span></td>
                        <td><span>456 pcs</span></td>
                        <td><span class="badge badge-success">Done</span></td>
                      </tr>
                      <tr>
                        <td><?= $unme ?></td>
                        <td><span>T-Shirt</span></td>
                        <td><span>1</span></td>
                        <td><span class="badge badge-success">Done</span></td>
                      </tr>
                    </tbody>
                  </table>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="row">
          <div class="col-lg-8">
            <div class="row">
              <div class="col-lg-6">
                <div class="card">
                  <div class="card-title">
                    <h4>Feedback</h4>
                  </div>
                  <div class="recent-comment">
                    <div class="media">
                      <div class="media-left">
                        <a href="#"><img alt="..." src="images/avatar/1.jpg" class="media-object" /></a>
                      </div>
                      <div class="media-body">
                        <h4 class="media-heading"><?= $unme ?></h4>
                        <p>Good</p>
                        <p>Keep it up</p>
                        <p class="comment-date">March 20, 2021</p>
                      </div>
                    </div>
                    <div class="media">
                      <div class="media-left">
                        <a href="#"><img alt="..." src="images/avatar/1.jpg" class="media-object" /></a>
                      </div>
                      <div class="media-body">
                        <h4 class="media-heading">john doe</h4>
                        <p>Cras sit amet nibh libero, in gravida nulla.</p>
                        <p class="comment-date">October 21, 2018</p>
                      </div>
                    </div>

                    <div class="media no-border">
                    </div>
                  </div>
                </div>
                <!-- /# card -->
              </div>
              <!-- /# column -->
              <div class="col-lg-6">
                <div class="card">
                  <div class="card-body">
                    <div class="year-calendar"></div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div class="col-lg-4">
            <div class="card">
              <div class="card-body">
                <h4 class="card-title">Todo</h4>
                <div class="card-content">
                  <div class="todo-list">
                    <div class="tdl-holder">
                      <div class="tdl-content">
                        <ul>
                          <li>
                            <label>
                              <input type="checkbox" /><i class="bg-primary"></i><span>Check dashboard</span>
                              <a href="#" class="ti-close"></a>
                            </label>
                          </li>
                          <li>
                        </ul>
                      </div>
                      <input type="text" class="tdl-new form-control" placeholder="Type here" />
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- End PAge Content -->
      </div>
      <!-- End Container fluid  -->
      <!-- footer -->
      <footer class="footer">
        © 2021 All rights reserved. Template designed by
        <a href="https://colorlib.com">Colorlib</a>
      </footer>
      <!-- End footer -->
    </div>
    <!-- End Page wrapper  -->
  </div>
  <!-- End Wrapper -->
  <!-- All Jquery -->
  <script src="js/lib/jquery/jquery.min.js"></script>
  <!-- Bootstrap tether Core JavaScript -->
  <script src="js/lib/bootstrap/js/popper.min.js"></script>
  <script src="js/lib/bootstrap/js/bootstrap.min.js"></script>
  <!-- slimscrollbar scrollbar JavaScript -->
  <script src="js/jquery.slimscroll.js"></script>
  <!--Menu sidebar -->
  <script src="js/sidebarmenu.js"></script>
  <!--stickey kit -->
  <script src="js/lib/sticky-kit-master/dist/sticky-kit.min.js"></script>
  <!--Custom JavaScript -->

  <!-- Amchart -->
  <script src="js/lib/morris-chart/raphael-min.js"></script>
  <script src="js/lib/morris-chart/morris.js"></script>
  <script src="js/lib/morris-chart/dashboard1-init.js"></script>

  <script src="js/lib/calendar-2/moment.latest.min.js"></script>
  <!-- scripit init-->
  <script src="js/lib/calendar-2/semantic.ui.min.js"></script>
  <!-- scripit init-->
  <script src="js/lib/calendar-2/prism.min.js"></script>
  <!-- scripit init-->
  <script src="js/lib/calendar-2/pignose.calendar.min.js"></script>
  <!-- scripit init-->
  <script src="js/lib/calendar-2/pignose.init.js"></script>

  <script src="js/lib/owl-carousel/owl.carousel.min.js"></script>
  <script src="js/lib/owl-carousel/owl.carousel-init.js"></script>
  <script src="js/scripts.js"></script>
  <!-- scripit init-->

  <script src="js/custom.min.js"></script>
</body>

</html>