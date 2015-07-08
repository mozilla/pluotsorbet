module J2ME {
  class WaitingContext {
    ctx: Context;
    private weight: number = 0;

    constructor(ctx: Context) {
      this.ctx = ctx;
      this.updateWeight(1);
    }

    updateWeight(elapsed: number) {
      this.weight += elapsed * this.ctx.priority;
    }

    static compare(a: WaitingContext, b: WaitingContext) {
      return a.weight - b.weight;
    }
  }

  export class ContextQueue {
    private arr: WaitingContext [] = [];
    private lastTime: number = 0;

    constructor() {
    }

    put(ctx: Context) {
      this.updateWeights(performance.now());
      this.arr.push(new WaitingContext(ctx));
    }

    private updateWeights(time: number) {
      var elapsed = time - this.lastTime;
      this.lastTime = time;
      for (var i = 0; i < this.arr.length; i++) {
        this.arr[i].updateWeight(elapsed);
      }
    }

    get(time: number): Context {
      if (this.arr.length === 0) {
        return null;
      }

      if (this.arr.length !== 1) {
        this.updateWeights(time);
        this.arr.sort(WaitingContext.compare);
      }

      return this.arr.pop().ctx;
    }

    getAll(): Context [] {
      this.updateWeights(performance.now());
      this.arr.sort(WaitingContext.compare);

      var ret = new Array(this.arr.length);
      for (var i = 0; i < this.arr.length; i++) {
        ret[i] = this.arr[i].ctx;
      }

      this.arr.length = 0;
      return ret;
    }

    remove(ctx: Context) {
      for (var i = 0; i < this.arr.length; i++) {
        if (ctx === this.arr[i].ctx) {
          this.arr.splice(i, 1);
          return;
        }
      }
    }

    hasMore() : boolean {
      return (this.arr.length !== 0);
    }
  }
}
