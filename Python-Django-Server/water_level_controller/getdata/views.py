# Create your views here.
import datetime
import time
from calendar import monthrange
from django.http import JsonResponse
from django.views.decorators.csrf import csrf_exempt
from rest_framework.parsers import JSONParser
from getdata.serializer import DataSerializer
from getdata.models import Data

Node_MCU_Data = {}
Emergency_Stop = 0
Force_Stop = 0
current_pump_status = 0
ontime = datetime.datetime.now()

ispumpon = 0


@csrf_exempt
def nodemcu(request):
    global Node_MCU_Data, Force_Stop, Emergency_Stop, current_pump_status, ontime, ispumpon
    if request.method == 'POST':
        data = JSONParser().parse(request)
        Node_MCU_Data = data

        if data['pumpstatus'] and ispumpon == 0:
            ontime = datetime.datetime.now()
            ispumpon = 1

        elif ispumpon and data['pumpstatus'] == 0:
            offtime = datetime.datetime.now()
            savedata = {'ontime': ontime, 'offtime': offtime}
            serializer = DataSerializer(data=savedata)
            if serializer.is_valid():
                serializer.save()
            ispumpon = 0

        current_pump_status = data['pumpstatus']
        resp = {'status': 'ok', 'Emergency_Stop': Emergency_Stop, 'Force_Stop': Force_Stop}
        # print("Response : ",resp)
        return JsonResponse(resp, status=200)


@csrf_exempt
def control(request):
    global Node_MCU_Data, Force_Stop, Emergency_Stop
    if request.method == 'POST':
        data = JSONParser().parse(request)
        if Force_Stop != data['forcestop']:
            Force_Stop = data['forcestop']
            Emergency_Stop = 0
        if data['emergencystatus'] == 1:
            Emergency_Stop = 1
            time.sleep(3)
            Emergency_Stop = 0
        resp = {'status': 'ok', 'pumpstatus': current_pump_status}
        return JsonResponse(resp, status=200)


@csrf_exempt
def status(request):
    global Node_MCU_Data, Force_Stop, Emergency_Stop
    if request.method == 'GET':
        return JsonResponse(Node_MCU_Data, status=200)


@csrf_exempt
def analytics(request):
    global Node_MCU_Data, Force_Stop, Emergency_Stop
    if request.method == 'GET':
        resp = Analysis()
        return JsonResponse(resp, status=200)


def Analysis():
    today = datetime.datetime.now()
    today_total = Data.objects.filter(ontime__year=today.year, ontime__month=today.month,
                                      ontime__day=today.day).values()
    month_total = Data.objects.filter(ontime__year=today.year, ontime__month=today.month).values()
    (x, iter) = monthrange(today.year, today.month)

    d_total = 0
    for i in today_total:
        d_total = d_total + (i['offtime'] - i['ontime']).total_seconds()
    d_total = ((d_total / 60) / 60)

    data_points = [0] * iter
    for day in month_total:
        data_points[day['offtime'].day] = data_points[day['offtime'].day] + (
                    ((day['offtime'] - day['ontime']).total_seconds()) / 60) / 60

    m_total = sum(data_points)
    total_units = m_total* 4.476

    final_value = dict(daytotal=d_total, monthtotal=m_total, datapoints=data_points,
                       totalunits=total_units)
    return final_value