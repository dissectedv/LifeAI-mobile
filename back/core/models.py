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

class CompromissoAtividade(models.Model):
    descricao = models.TextField(db_column='descricao')
    done = models.BooleanField(db_column='done', default=False)
    compromisso = models.ForeignKey('compromisso', db_column='id_compromisso', on_delete=models.CASCADE, related_name='atividades')
    class Meta:
        db_table = 'compromisso_atividade'
        ordering = ['id']

    def __str__(self):
        status = "✔️" if self.done else "❌"
        return f"{status} {self.descricao}"


class pontuacao_compromisso(models.Model):
    compromisso = models.OneToOneField('compromisso', on_delete=models.CASCADE, related_name='pontuacao')
    qtd_total_atv = models.PositiveIntegerField(db_column='qtd_total_atv', default=0)
    qtd_atv_done = models.PositiveIntegerField(db_column='qtd_atv_done', default=0)
    porcentagem = models.FloatField(db_column='porcentagem', default=0)
    criado_em = models.DateTimeField(auto_now_add=True)

    class Meta:
        db_table = 'pontuacao_compromisso'
        ordering = ['id']

    def __str__(self):
        return f"Pontuação - Compromisso #{self.compromisso.id}: {self.porcentagem:.2f}%"

class compromisso(models.Model):
    titulo = models.CharField(db_column='titulo', max_length=200, null=False)
    data = models.DateField(db_column='data', null=False)
    hora_inicio = models.TimeField(db_column='hora_inicio', null=False)
    hora_fim = models.TimeField(db_column='hora_fim', null=False)
    concluido = models.BooleanField(db_column='concluido', default=False)
    id_usuario = models.ForeignKey(User, db_column='id_usuario', on_delete=models.CASCADE)

    class Meta:
        db_table = 'compromisso'
        unique_together = (('id_usuario', 'data', 'hora_inicio'),)
        ordering = ['-data', 'hora_inicio']

    def __str__(self):
        return f"{self.titulo} - {self.data} ({self.hora_inicio} às {self.hora_fim})"
