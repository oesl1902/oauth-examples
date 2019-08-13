from django.urls import path
from . import views

urlpatterns = [
    path('', views.oauth, name='oauth'),
    path('otp-data', views.otpData, name='otp-data'),
    path('otp-form', views.otpForm, name='otp'),
    path('redirect', views.getAuthorizeCode, name='authorize-code'),
    path('access-token', views.getAccessToken, name='access-token')
]