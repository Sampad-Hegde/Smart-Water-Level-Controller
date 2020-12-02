from rest_framework import serializers

from getdata.models import Data


class DataSerializer(serializers.ModelSerializer):
    class Meta:
        model = Data
        # fields = ['Ondate', 'Ontime',  'Offdate', 'Offtime']
        fields = '__all__'