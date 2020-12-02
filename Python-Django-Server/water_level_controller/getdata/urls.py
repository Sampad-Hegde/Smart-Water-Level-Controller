from django.urls import path
from getdata import views

urlpatterns = [
    path('getdata', views.nodemcu, name='nodemcu'),
    path('analytics', views.analytics, name='analytics'),
    path('status', views.status, name='status'),
    path('control', views.control, name='control'),
]
