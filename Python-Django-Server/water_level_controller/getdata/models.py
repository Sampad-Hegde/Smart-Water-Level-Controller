from django.db import models

# Create your models here.

class Data(models.Model):
    ontime = models.DateTimeField(auto_now=False, auto_now_add=False)
    offtime = models.DateTimeField(auto_now=False, auto_now_add=False)
