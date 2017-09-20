<?php

namespace App;

interface Renderable
{
    public function getTitle(): string;

    public function render(): string;
}