from django.contrib.auth.models import User
from rest_framework import serializers
from .models import Expense

class RegisterSerializer(serializers.ModelSerializer):
    class Meta:
        model = User
        fields = ['username', 'email', 'password']
        extra_kwargs = {'password': {'write_only': True}}

    def create(self, validated_data):
        user = User.objects.create_user(**validated_data)
        return user
    
class ExpenseSerializer(serializers.ModelSerializer):
    class Meta:
        model = Expense
        fields = ['id', 'title', 'amount', 'category', 'date']

    def validate_amount(self, value):
        if value < 0:
            raise serializers.ValidationError("Expense amount cannot be negative.")
        return value