from django.urls import path
from .views import RegisterView, ExpenseListCreateView

urlpatterns = [
    
    path('register/', RegisterView.as_view(), name='register'),
    path('expenses/', ExpenseListCreateView.as_view(), name='expenses'),
#     path('api/', include('users.urls')),

]