from django.http import JsonResponse

def health_check(request):
    """Endpoint simples de verificação de saúde"""
    return JsonResponse({"status": "ok"})