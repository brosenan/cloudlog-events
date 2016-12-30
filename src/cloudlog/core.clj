(ns cloudlog.core)

(defmulti generate-body (fn [conds num] (class (first conds))))
(defmethod generate-body  clojure.lang.IPersistentVector [conds num]
  [(vec (rest (first conds)))])

(defmethod generate-body  clojure.lang.ISeq [conds num]
  (seq (concat (first conds) [(generate-body (rest conds) num)])))

(defn generate-rule-func [rulename source-fact source-rule conds num]
  (let [funcname (symbol (str (name rulename) "-" num))]
    `(do (def ~funcname ^{:source-fact ~[(first source-fact) (count (rest source-fact))]}
           (fn [[~@(rest source-fact)]]
             ~(generate-body conds num))))))

(defmacro --> [rulename source-fact & conds]
  (generate-rule-func rulename source-fact nil conds 0))