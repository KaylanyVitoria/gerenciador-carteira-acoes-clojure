(ns kaymarket.cli.cli
  (:require [kaymarket.cli.cli-helper :as h]
            [clojure.string :as str])
  (:gen-class))

;; ==========================================
;;   Função Genérica de Compra e Venda
;; ==========================================
(defn realizar-operacao [tipo-operacao]
  (let [endpoint (if (= tipo-operacao :compra) "/compra" "/venda")
        titulo   (if (= tipo-operacao :compra) "COMPRAR" "VENDER")]

    (println (str "\n--- " titulo " ACAO ---"))
    (print "Digite o codigo (ex: PETR4): ") (flush)

    (let [codigo (h/ler-entrada)]
      (if (str/blank? codigo)
        (println "Operação cancelada.")

        (let [dados-acao (h/buscar-preco-real codigo)]

          (if (:erro dados-acao)
            (println "ERRO:" (:erro dados-acao))

            (let [{preco :ultimo-preco nome :nome} dados-acao
                  agora (h/data-hora-atual)]

              (println "Ação:" codigo)
              (println "Preço: R$" preco "| Data:" agora)

              (print "Quantidade: ") (flush)

              (if-let [qtd (h/ler-inteiro)]
                (if (pos? qtd)

                  (let [resp (h/post-json endpoint
                                          {:codigo codigo
                                           :quantidade qtd
                                           :preco preco
                                           :data agora})]

                    (if (:erro resp)
                      (println "ERRO:" (:erro resp))
                      (println titulo "realizada com sucesso! Total pago: R$"
                               (h/formatar-dinheiro (* preco qtd)))))

                  (println "Quantidade deve ser positiva."))

                (println "Operação cancelada.")))))))))

(defn acao-comprar [] (realizar-operacao :compra))
(defn acao-vender  [] (realizar-operacao :venda))

;; ==================================================
;;            EXTRATO
;; ==================================================
(defn obter-e-imprimir-extrato [inicio-param fim-param]
  (let [query   (str "/extrato?inicio=" inicio-param "&fim=" fim-param)
        extrato (h/get-json query)]

    (if (or (:erro extrato) (empty? extrato))
      (println "Sem transações para este período.")
      (do
        (println "\n--- EXTRATO FINANCEIRO (Período Solicitado) ---")
        (h/imprimir-lista-extrato extrato)))))

(defn extrato-por-periodo []
  (print "Digite a data de INÍCIO (YYYY-MM-DD): ") (flush)
  (let [inicio (h/ler-entrada)]
    (if (str/blank? inicio)
      (println "Operação cancelada.")

      (do
        (print "Digite a data de FIM (YYYY-MM-DD): ") (flush)
        (let [fim (h/ler-entrada)]
          (if (str/blank? fim)
            (println "Operação cancelada.")

            (let [start (str inicio "T00:00:00Z")
                  end   (str fim    "T23:59:59Z")]
              (obter-e-imprimir-extrato start end))))))))

(defn acao-extrato []
  (println "\n--- CONSULTAR EXTRATO ---")
  (println "1. Extrato Completo")
  (println "2. Extrato por Período")
  (print "Escolha uma opção: ") (flush)

  (let [op (h/ler-entrada)]
    (case op
      "1" (obter-e-imprimir-extrato
           "2020-01-01T00:00:00Z"
           "2030-12-31T23:59:59Z")

      "2" (extrato-por-periodo)

      (println "Opção inválida."))))

;; ==================================================
;;            SALDO
;; ==================================================
(defn acao-saldo []
  (println "\n--- SALDO E PATRIMONIO ---")
  (println "(Consultando valores de mercado...)")

  (let [resp (h/get-json "/carteira")]
    (if (or (:erro resp) (empty? resp))
      (println "Carteira vazia.")
      (h/imprimir-lista-saldo resp 0.0))))

;; ==================================================
;;            MENU PRINCIPAL
;; ==================================================
(defn menu-principal []
  (println "\n-----------------------------")
  (println "      KAYMARKET AÇÕES")
  (println "-----------------------------")
  (println "1. Comprar Ações")
  (println "2. Vender Ações")
  (println "3. Consultar Extrato")
  (println "4. Ver Saldo da Carteira")
  (println "5. Sair")
  (print "Escolha uma opção: ") (flush)

  (let [op (h/ler-entrada)]
    (case op
      "1" (do (acao-comprar) (recur))
      "2" (do (acao-vender)  (recur))
      "3" (do (acao-extrato) (recur))
      "4" (do (acao-saldo)   (recur))
      "5" (println "Saindo... Até logo!") 
      (do (println "Opção inválida.") (recur)))))


(defn -main []
  (menu-principal))
