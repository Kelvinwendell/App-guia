# SPEC-010: Hospedar a API de graça (Oracle Cloud "Always Free")

## Status
BLOCKED (requer ação externa — conta na Oracle Cloud)

## Prioridade
CRÍTICO

## Decisão
Trocado de Render para Oracle Cloud a pedido do dono do projeto (Render dorme após
inatividade; Oracle dá uma VM real, grátis para sempre, sem esse problema — ver comparativo
na conversa). Troca-off: sem deploy automático a cada `git push` (isso o Render tinha de
graça); aqui a atualização é manual (ou dá pra montar depois, ver nota no fim) via SSH.

## Execução
**Não autônoma.** Requer conta do dono do projeto (pede cartão de crédito só para
verificação de identidade — não cobra nada dentro do limite "Always Free"). Eu posso ajudar a
revisar comandos, diagnosticar erro de conexão/firewall, e preparar scripts, mas não posso
criar a conta nem acessar a VM.

## Por que resolve sem custo
Oracle Cloud "Always Free" inclui (para sempre, não é trial): até 4 OCPUs + 24GB RAM em
instâncias **Ampere A1 (ARM)**, ou 2 instâncias `VM.Standard.E2.1.Micro` (AMD, 1 OCPU/1GB
cada). Dá pra rodar o `backend/Dockerfile` continuamente sem "dormir" e sem custo.

## Checklist de execução (manual, pelo dono do projeto)

### 1. Criar a conta e a instância
- [ ] Criar conta em https://signup.oraclecloud.com (pede cartão, não cobra no Always Free).
- [ ] No console, ir em **Compute → Instances → Create Instance**.
- [ ] **Image**: Canonical Ubuntu (a mais recente LTS disponível).
- [ ] **Shape**: trocar para **Ampere (ARM)** → `VM.Standard.A1.Flex` → configurar 2 OCPUs /
      12GB RAM (dá margem e ainda deixa metade da cota livre para o futuro).
- [ ] **Networking**: deixar a VCN padrão que a Oracle sugere criar junto.
- [ ] **SSH keys**: deixar a Oracle gerar um par de chaves e **baixar a chave privada
      (`.key`)** — é a única forma de acessar depois. Guardar em local seguro.
- [ ] **Create**.

### 2. Liberar a porta da API (duas camadas, as duas são necessárias)
- [ ] No console: **VCN do projeto → Security Lists → Default Security List → Add Ingress
      Rule**: Source CIDR `0.0.0.0/0`, protocolo TCP, porta de destino `8080`.
- [ ] Depois de conectar na VM (passo 3), também liberar no firewall do próprio Ubuntu:
      ```bash
      sudo iptables -I INPUT -p tcp --dport 8080 -j ACCEPT
      sudo netfilter-persistent save
      ```
      (pegadinha clássica da Oracle: só abrir na VCN não basta, o firewall da própria VM
      também bloqueia por padrão).

### 3. Conectar via SSH e instalar Docker
- [ ] Pegar o IP público da instância na tela de detalhes dela.
- [ ] ```bash
      chmod 400 caminho/para/a-chave.key
      ssh -i caminho/para/a-chave.key ubuntu@IP_PUBLICO_DA_VM
      ```
- [ ] Dentro da VM, instalar Docker:
      ```bash
      sudo apt-get update
      sudo apt-get install -y docker.io docker-compose-plugin
      sudo usermod -aG docker $USER
      # sair e entrar de novo no SSH para o grupo "docker" valer
      ```

### 4. Subir a API
- [ ] Clonar o repositório (ou só copiar a pasta `backend/`):
      ```bash
      git clone https://github.com/Kelvinwendell/App-guia.git
      cd App-guia/backend
      ```
- [ ] Rodar o container com as variáveis de produção (usar a `MONGODB_URI` do Atlas da
      SPEC-009; gerar `JWT_SECRET` com `openssl rand -base64 48`, nunca o valor-padrão do
      repositório — ver SPEC-001):
      ```bash
      sudo docker build -t aguia-inova-api .
      sudo docker run -d --name aguia-api --restart unless-stopped -p 8080:8080 \
        -e MONGODB_URI="mongodb+srv://usuario:senha@cluster.../aguiainova?retryWrites=true&w=majority" \
        -e JWT_SECRET="o-segredo-gerado-com-openssl" \
        -e JWT_EXPIRATION_MINUTES=480 \
        -e APP_SEED_ENABLED=false \
        -e APP_ALLOW_ROLE_SELECTION=false \
        -e AI_ENABLED=true \
        -e AI_PROVIDER=gemini \
        -e AI_API_KEY="" \
        -e AI_MODEL=gemini-3.6-flash \
        aguia-inova-api
      ```
      `--restart unless-stopped` garante que o container volta sozinho se a VM reiniciar.

### 5. Validar
- [ ] Da sua própria máquina (não da VM): `curl http://IP_PUBLICO_DA_VM:8080/actuator/health`
      — esperado `{"status":"UP"}`.

## Nota sobre atualização futura
Sem o deploy automático que o Render dava de graça, atualizar a API depois de um `git push`
vira: `ssh` na VM, `git pull`, `docker build` de novo, `docker stop/rm` + `docker run` de
novo. Se isso incomodar, dá pra montar um workflow de GitHub Actions que faz esse SSH+deploy
sozinho a cada push na branch principal — posso preparar isso depois, é uma spec própria.

## Resultado
(preencher quando a VM estiver criada e a API no ar; posso ajudar a diagnosticar erro de
conexão/firewall quando você chegar nessa etapa — essa parte eu consigo testar de fora, uma
vez que a VM tenha IP público normal, sem a restrição de DNS que bloqueou o teste do Atlas
direto deste sandbox)
