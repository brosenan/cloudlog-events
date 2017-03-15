(ns perm.Qmbeprn3Lj4sqvQZpkSmckBtFHTS4QPLAso7KJELcoa2Hj [:require [permacode.symbols :as symbols]] [:require [permacode.core]] [:require [clojure.set :as set]] [:require [clojure.string :as string]] [:require [perm.QmQodG29316hRLwuJVGpCP3CW7VCJEE5KTpK6ABDBDUT1H :as unify]] [:require [perm.QmYANgLvtDmSc2PM5nJ2w2ZEHTs5R1tq71rW49LhnUX86P :as graph]])(permacode.core/pure (declare generate-rule-func) (defmulti propagate-symbols (fn [cond symbols] (first cond)) :default :no-bindings) (defmethod propagate-symbols :no-bindings [cond symbols] symbols) (defn binding-symbols [bindings cond] (symbols/symbols (map bindings (range 0 (count bindings) 2)))) (defmethod propagate-symbols (quote let) [cond symbols] (set/union symbols (binding-symbols (second cond) cond))) (defmethod propagate-symbols (quote for) [cond symbols] (set/union symbols (binding-symbols (second cond) cond))) (defmulti process-conds (fn [conds symbols] (class (first conds)))) (defmethod process-conds clojure.lang.IPersistentVector [conds symbols] (let [target (first conds) target-name (first target)] (if (= (count conds) 1) (do [[(vec (rest target))] {:target-fact [target-name (count (rest target))]}]) (let [[func meta] (generate-rule-func (first conds) (rest conds) symbols) key (second target) params (vec (set/intersection symbols (symbols/symbols func))) missing (set/difference (symbols/symbols key) symbols) meta {:continuation (with-meta (clojure.core/seq (clojure.core/concat (clojure.core/list (quote clojure.core/fn)) (clojure.core/list (clojure.core/apply clojure.core/vector (clojure.core/seq (clojure.core/concat (clojure.core/list (clojure.core/apply clojure.core/vector (clojure.core/seq (clojure.core/concat (clojure.core/list (quote $key$)) params)))))))) (clojure.core/list func))) meta)}] (when-not (empty? missing) (permacode.core/error "variables " missing " are unbound in the key for " (first target))) [(clojure.core/apply clojure.core/vector (clojure.core/seq (clojure.core/concat (clojure.core/list (clojure.core/apply clojure.core/vector (clojure.core/seq (clojure.core/concat (clojure.core/list key) params))))))) meta])))) (defmethod process-conds clojure.lang.ISeq [conds symbols] (let [cond (first conds) [body meta] (process-conds (rest conds) (propagate-symbols cond symbols)) body (seq (concat cond [body])) meta (if (string/starts-with? (name (first cond)) "by") (assoc meta :checked true) meta)] (if (= (first cond) (quote for)) [(clojure.core/seq (clojure.core/concat (clojure.core/list (quote clojure.core/apply)) (clojure.core/list (quote clojure.core/concat)) (clojure.core/list body))) meta] [body meta]))) (defn fact-name [fact] (if (keyword? fact) fact (if (symbol? fact) (clojure.core/seq (clojure.core/concat (clojure.core/list (quote clojure.core/keyword)) (clojure.core/list (clojure.core/seq (clojure.core/concat (clojure.core/list (quote clojure.core/->)) (clojure.core/list fact) (clojure.core/list (quote clojure.core/meta)) (clojure.core/list :ns) (clojure.core/list (quote clojure.core/str))))) (clojure.core/list (clojure.core/seq (clojure.core/concat (clojure.core/list (quote clojure.core/->)) (clojure.core/list fact) (clojure.core/list (quote clojure.core/meta)) (clojure.core/list :name)))))) (keyword (-> fact meta :ns str) (-> fact meta name))))) (defn generate-rule-func [source-fact conds ext-symbols] (let [symbols (set/difference (symbols/symbols (rest source-fact)) ext-symbols) [body meta] (process-conds conds (set/union symbols ext-symbols)) meta (merge meta {:source-fact [(fact-name (first source-fact)) (count (rest source-fact))]}) vars (set symbols) term-has-vars (fn [term] (not (empty? (set/intersection (symbols/symbols term) vars)))) travmap (unify/traverse (vec (rest source-fact)) (constantly true)) [conds bindings] (unify/conds-and-bindings (map identity travmap) term-has-vars) func (clojure.core/seq (clojure.core/concat (clojure.core/list (quote clojure.core/fn)) (clojure.core/list (clojure.core/apply clojure.core/vector (clojure.core/seq (clojure.core/concat (clojure.core/list (quote $input$)))))) (clojure.core/list (clojure.core/seq (clojure.core/concat (clojure.core/list (quote if)) (clojure.core/list (clojure.core/seq (clojure.core/concat (clojure.core/list (quote clojure.core/and)) conds))) (clojure.core/list (clojure.core/seq (clojure.core/concat (clojure.core/list (quote clojure.core/let)) (clojure.core/list bindings) (clojure.core/list body)))) (clojure.core/list (clojure.core/apply clojure.core/vector (clojure.core/seq (clojure.core/concat)))))))))] [func meta])) (defn validate-rule [metadata] (loop [metadata metadata link 0] (when-not (:checked metadata) (permacode.core/error "Rule is insecure. Link " link " is not checked.")) (when (:continuation metadata) (recur (-> metadata :continuation meta) (inc link))))) (defmacro defrule [rulename args source-fact & body] (let [conds (concat body [(clojure.core/apply clojure.core/vector (clojure.core/seq (clojure.core/concat (clojure.core/list (keyword (str *ns*) (name rulename))) args)))]) [func meta] (generate-rule-func source-fact conds #{})] (validate-rule meta) (clojure.core/seq (clojure.core/concat (clojure.core/list (quote def)) (clojure.core/list rulename) (clojure.core/list (clojure.core/seq (clojure.core/concat (clojure.core/list (quote clojure.core/with-meta)) (clojure.core/list func) (clojure.core/list (merge meta {:ns *ns*, :name (str rulename)}))))))))) (defn append-to-keyword [keywd suffix] (keyword (namespace keywd) (str (name keywd) suffix))) (defmacro defclause [clausename pred args-in args-out & body] (let [source-fact (clojure.core/apply clojure.core/vector (clojure.core/seq (clojure.core/concat (clojure.core/list (append-to-keyword pred "?")) (clojure.core/list (quote $unique$)) args-in))) conds (concat body [(clojure.core/apply clojure.core/vector (clojure.core/seq (clojure.core/concat (clojure.core/list (append-to-keyword pred "!")) (clojure.core/list (quote $unique$)) args-out)))]) [func meta] (generate-rule-func source-fact conds #{})] (clojure.core/seq (clojure.core/concat (clojure.core/list (quote def)) (clojure.core/list clausename) (clojure.core/list (clojure.core/seq (clojure.core/concat (clojure.core/list (quote clojure.core/with-meta)) (clojure.core/list func) (clojure.core/list (merge meta {:ns *ns*, :name (str clausename)}))))))))) (defn with* [seq] (apply merge-with set/union (for [fact seq] (let [fact-name (first fact) metadata (meta fact) arity (-> fact rest count)] {[fact-name arity] #{(with-meta (vec (rest fact)) metadata)}})))) (defn simulate* [rule factmap] (let [source-fact (-> rule meta :source-fact) input-set (factmap source-fact) after-first (into #{} (apply concat (map rule input-set))) cont (-> rule meta :continuation)] (if cont (let [next-rules (map cont after-first)] (into #{} (reduce concat (for [next-rule next-rules] (simulate* (with-meta next-rule (meta cont)) factmap))))) after-first))) (defn simulate-with [rule & facts] (simulate* rule (with* facts))) (defmulti fact-table (fn [[name arity]] (class name))) (defmethod fact-table clojure.lang.Named [[name arity]] (str (namespace name) "/" (clojure.core/name name))) (defmethod fact-table clojure.lang.IFn [[name arity]] (let [ns (-> name meta :ns) name (-> name meta :name)] (str ns "/" name))) (prefer-method fact-table clojure.lang.Named clojure.lang.IFn) (defmacro by [set body] (clojure.core/seq (clojure.core/concat (clojure.core/list (quote clojure.core/when)) (clojure.core/list (clojure.core/seq (clojure.core/concat (clojure.core/list (quote clojure.core/contains?)) (clojure.core/list (clojure.core/seq (clojure.core/concat (clojure.core/list (quote clojure.core/->)) (clojure.core/list (quote $input$)) (clojure.core/list (quote clojure.core/meta)) (clojure.core/list :writers)))) (clojure.core/list set)))) (clojure.core/list body)))) (defmacro by-anyone [body] body) (defn rule-cont [rule] (loop [func rule res nil] (if-let [cont (-> func meta :continuation)] (recur cont (cons func res)) (reverse (cons func res))))) (defn rule-target-fact [rule] (let [conts (rule-cont rule)] (some identity (map (fn [x] (-> x meta :target-fact)) conts)))) (defn sort-rules [rules] (let [inv-graph (reduce graph/merge-graph {} (for [rule rules] (let [conts (rule-cont rule)] {(rule-target-fact rule) (set (map (fn [x] (-> x meta :source-fact)) conts))}))) inv-sort (graph/toposort inv-graph) target-fact-map (reduce merge {} (for [rule rules] (let [conts (rule-cont rule)] {(rule-target-fact rule) rule})))] (->> (reverse inv-sort) (map target-fact-map) (filter identity)))) (defn simulate-rules-with [rules & facts] (loop [rules (sort-rules rules) facts (with* facts)] (if (empty? rules) facts (recur (rest rules) (assoc facts (rule-target-fact (first rules)) (simulate* (first rules) facts)))))))