from django.db import models
from django.contrib.auth.models import User

class imc(models.Model):
    data_consulta = models.DateField(db_column='data_consulta')
    idade = models.IntegerField(db_column='idade', null=False)
    sexo = models.CharField(db_column='sexo', max_length=10, null=False)
    peso = models.FloatField(db_column='peso', null=False)
    altura = models.FloatField(db_column='altura', null=False)
    imc_res = models.FloatField(db_column='imc_res', null=True, blank=True)
    classificacao = models.CharField(db_column='classificacao', max_length=30, null=True, blank=True)
    id_usuario = models.ForeignKey(User, db_column='id_usuario', on_delete=models.CASCADE)

    class Meta:
        db_table = 'imc'
        ordering = ['id']

class dica(models.Model):
    classificacao = models.CharField(db_column='classificacao', max_length=100, null=False)
    dica_imc = models.TextField(db_column='dica', null=False)

    class Meta:
        db_table = 'dica'
        ordering = ['id']

class imc_user_base(models.Model):
    nome = models.CharField(db_column='nome', max_length=10, null=False)
    idade = models.IntegerField(db_column='idade', null=False)
    peso = models.FloatField(db_column='peso', null=False)
    altura = models.FloatField(db_column='altura', null=False)
    sexo = models.CharField(db_column='sexo', max_length=10, null=False)
    imc_res = models.FloatField(db_column='imc_res', null=True, blank=True)
    classificacao = models.CharField(db_column='classificacao', max_length=30, null=True, blank=True)
    objetivo = models.CharField(db_column='objetivo', max_length=200, null=False)
    id_usuario = models.ForeignKey(User, db_column='id_usuario', on_delete=models.CASCADE)

    class Meta:
        db_table = 'imc_user_base'
        ordering = ['id']

class checklist(models.Model):
    data = models.DateField(db_column='data', null=False)
    created_at = models.DateTimeField(db_column='created_at', auto_now_add=True)
    id_usuario = models.ForeignKey(User, db_column='id_usuario', on_delete=models.CASCADE)
    class Meta:
        db_table = 'checklist'
        unique_together = ('id_usuario', 'data')
        ordering = ['-data']

    def __str__(self):
        return f"Checklist de {self.id_usuario.username} - {self.data}"

class atividade(models.Model):
    descricao = models.TextField(db_column='descricao')
    done = models.BooleanField(db_column='done', default=False)
    checklist = models.ForeignKey(checklist, db_column='id_checklist', on_delete=models.CASCADE)
    class Meta:
        db_table = 'atividade'
        ordering = ['id']

    def __str__(self):
        status = "✔️" if self.done else "❌"
        return f"{status} {self.descricao}"


class pontuacao_check(models.Model):
    checklist = models.OneToOneField('checklist', on_delete=models.CASCADE, related_name='pontuacao')
    qtd_total_atv = models.PositiveIntegerField(db_column='qtd_total_atv', default=False)
    qtd_atv_done = models.PositiveIntegerField(db_column='qtd_atv_done', default=False)
    porcentagem = models.FloatField(db_column='porcentagem', default=False)

    criado_em = models.DateTimeField(auto_now_add=True)

    class Meta:
        db_table = 'pontuacao_check'
        ordering = ['id']

    def __str__(self):
        return f"Pontuação - Checklist #{self.checklist.id}: {self.porcentagem:.2f}%"