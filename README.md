# 📱 FisioAging - Mobile Application

O **FisioAging** é uma aplicação mobile voltada à área da saúde, desenvolvida como Trabalho de Conclusão de Curso (TCC) para a graduação em Análise e Desenvolvimento de Sistemas na **Universidade Federal do Paraná (UFPR)**. O principal objetivo do ecossistema é medir e acompanhar a vulnerabilidade de idosos de forma prática e precisa.

---

## 🛠️ Meu Escopo de Atuação & Tecnologias
Sendo um projeto multidisciplinar de grande porte, a solução conta com uma API/Backend e um painel Web desenvolvidos por outros membros da equipe. 

**Eu fui o principal responsável pelo ciclo de vida do aplicativo Android Nativo do início ao fim**, englobando:
* **Desenvolvimento Nativo:** Criação da interface de usuário e regras de negócio utilizando **Kotlin** com padrões modernos do ecossistema Android Jetpack.
* **Leitura de Sensores:** Implementação da lógica de captura, tratamento e persistência de dados de hardware através dos sensores de **acelerômetro e giroscópio** do dispositivo para mensurar testes físicos em idosos (como testes de marcha estacionária e UTT).
* **Consumo de APIs:** Integração do aplicativo à API REST do ecossistema via Retrofit, tratando fluxos de autenticação (Tokens JWT/Interceptors) e sincronização offline-first para upload dos testes coletados.

### 🧠 Tecnologias Utilizadas no Módulo Mobile:
* **Linguagem Principal:** Kotlin
* **Arquitetura de UI:** Activities, Custom Layouts e Componentes XML modernos
* **Conectividade:** Retrofit, OkHttp e Auth Interceptors
* **Sensores:** Android SensorManager (Sensor.TYPE_ACCELEROMETER, Sensor.TYPE_GYROSCOPE)

---

## 📈 Metodologia, Engenharia de Software e GitFlow
O desenvolvimento deste projeto seguiu rigorosamente práticas ágeis baseadas em **Scrum**, utilizando a divisão de tarefas por **Histórias de Usuário (HUs)**.

Neste repositório, mantive o histórico completo de branches originais para demonstrar o fluxo de **GitFlow** adotado pela equipe. Cada funcionalidade desenvolvida (como autenticação, telas de histórico ou execução de testes) foi mapeada em branches de features específicas vinculadas às suas respectivas HUs (ex: `feature/TCC-HU001`, `feature/TCC-HU005`), garantindo a rastreabilidade do código.

---

## 🛠️ Como Executar o Projeto Localmente

### Pré-requisitos
* Android Studio (versão Ladybug ou superior recomendada)
* SDK do Android instalada (API 31 ou superior)
* Um dispositivo físico Android (altamente recomendado para testar os sensores de movimento) ou Emulador com suporte a sensores.

### Passo a Passo
1. Clone este repositório no seu ambiente de desenvolvimento:
   ```bash
   git clone [https://github.com/CristhianKindermann/fisioaging-mobile.git](https://github.com/CristhianKindermann/fisioaging-mobile.git)

2. Abra o Android Studio e selecione a opção Open an Existing Project.

3. Selecione a pasta raiz do projeto clonado.

4. Aguarde a sincronização e download das dependências do Gradle.

5. Conecte seu dispositivo Android via depuração USB e clique em Run (Shift + F10).

Nota: Este repositório é um fork autorizado e focado no módulo mobile derivado do ecossistema original da organização TCC-IDOSOS.
